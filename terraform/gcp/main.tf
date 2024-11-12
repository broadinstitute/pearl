provider "google" {
  project     = var.project
  region      = var.region
}

data "google_client_config" "provider" {}

# state is stored remotely in GCS bucket
terraform {
  backend "gcs" {
    bucket = "broad-juniper-terraform-remote-state"
    prefix = "juniper"
  }
}
