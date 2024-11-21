
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

# resource "google_project_iam_binding" "juniper_cloudbuild_service_account_cluster_view_binding" {
#   project = var.project
#
#   role    = "roles/container.clusterViewer"
#   members = [
#     "serviceAccount:${google_service_account.cluster_service_account.email}"
#   ]
# }

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
    peered_network = google_compute_network.juniper_network.id
  }
  depends_on = [google_service_networking_connection.private_vpc_connection]
}

resource "google_cloudbuild_trigger" "dev_auto_deploy_on_tag_push" {
  count = var.environment == "dev" ? 1 : 0

  location = "us-central1"

  github {
    owner = "broadinstitute"
    name = "juniper"
    push {
      tag = "[0-9]+.[0-9]+.[0-9]+"
    }
  }

  service_account = "projects/${var.project}/serviceAccounts/${google_service_account.juniper_cloudbuild_service_account.email}"

  substitutions = {
    _ENV = var.environment
    _NAMESPACE = var.k8s_namespace
  }

  filename = "cloudbuild.yaml"
}
#
# resource "google_cloudbuild_trigger" "prod_manual_deploy" {
#   count = var.environment == "prod" ? 1 : 0
#
#   location = "us-central1"
#
#   github {
#     owner = "broadinstitute"
#     name = "juniper"
#   }
#
#   substitutions = {
#     ENV = var.environment
#     NAMESPACE = var.k8s_namespace
#   }
#
#   approval_config {
#     approval_required = true
#   }
#
#   filename = "cloud_build_deploy.yaml"
# }
