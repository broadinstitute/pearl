
resource "google_service_account" "juniper_cloudbuild_service_account" {
  account_id   = "juniper-cloudbuild-sa"
  display_name = "juniper-cloudbuild-sa"
  description  = "Cloud build service account"

  project = var.project
}

#needs access to the k8s cluster
resource "google_project_iam_binding" "juniper_cloudbuild_service_account_gke_binding" {
  project = var.project

  role    = "roles/container.developer"
  members = [
    "serviceAccount:${google_service_account.juniper_cloudbuild_service_account.email}"
  ]
}

resource "google_cloudbuild_worker_pool" "juniper-deployment-worker-pool" {
  name     = "juniper-deployment-worker-pool"
  project  = var.project
  location = var.region
  worker_config {
    disk_size_gb   = 100
    machine_type   = "e2-medium"
    no_external_ip = true
  }
  network_config {
    peered_network = google_compute_network.cloud_build_network.id
  }
  depends_on = [google_service_networking_connection.private_vpc_connection]
}

resource "google_cloudbuild_trigger" "dev_auto_deploy_on_tag_push" {
  count = var.environment == "dev" ? 1 : 0

  name = "deploy-juniper"

  location = "us-central1"

  github {
    owner = "broadinstitute"
    name = "juniper"
    push {
      tag = "[0-9]+.[0-9]+.[0-9]+"
    }
  }

  build {

    step {
      name = "gcr.io/cloud-builders/git"
      id = "Clone repository"
      args = ["clone", "--depth", "1", "--branch", "$TAG_NAME", "https://github.com/broadinstitute/juniper.git", "/workspace/juniper" ]
    }

    step {
      name = "gcr.io/cloud-builders/docker"
      id = "Wait for images to be published"
      entrypoint = "bash"
      args = [
        "-c",
        <<EOT
function wait_for_image () {
    echo "attempting to pull $1..."
    until docker pull $1
    do
        echo "image $1 not found..."
        sleep 30
        echo "attempting to pull $1..."
    done
    echo "found $1!"
}

wait_for_image "us-central1-docker.pkg.dev/broad-juniper-eng-infra/juniper/juniper-admin:$TAG_NAME"
wait_for_image "us-central1-docker.pkg.dev/broad-juniper-eng-infra/juniper/juniper-participant:$TAG_NAME"
        EOT
      ]
    }

    step {
      name = "gcr.io/google.com/cloudsdktool/cloud-sdk"
      id = "Deploy to k8s"
      dir = "/workspace/juniper/terraform/gcp/k8s"
      entrypoint = "bash"
      env = ["CLOUDSDK_COMPUTE_ZONE=us-central1", "CLOUDSDK_CONTAINER_CLUSTER=juniper-cluster"]
        args = [
            "-c",
            <<EOT
gcloud container clusters get-credentials juniper-cluster --zone us-central1
curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash
helm upgrade -f environments/$_ENV.yaml juniper . --namespace $_NAMESPACE --set appVersion=$TAG_NAME --atomic --wait
            EOT
        ]
    }

    options {
      logging = "CLOUD_LOGGING_ONLY"
      worker_pool = google_cloudbuild_worker_pool.juniper-deployment-worker-pool.id
    }

    timeout = "1500s" # 25 minutes so that the images have time to be built and pushed
  }

  service_account = "projects/${var.project}/serviceAccounts/${google_service_account.juniper_cloudbuild_service_account.email}"

  substitutions = {
    _ENV = var.environment
    _NAMESPACE = var.k8s_namespace
  }

  approval_config {
    approval_required = var.environment == "prod" ? true : false
  }
}
