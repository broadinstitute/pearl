# Creates the secrets that our application needs to run.
# These secrets need to be manually populated.

resource "google_secret_manager_secret" "tdr_sa_creds" {
  secret_id = "tdr-sa-creds"
  replication {
    auto {}
  }
}

resource "google_secret_manager_secret" "tdr_storage_account_key" {
  secret_id = "tdr-storage-account-key"
  replication {
    auto {}
  }

}

resource "google_secret_manager_secret" "dsm_secret" {
  secret_id = "dsm-secret"
  replication {
    auto {}
  }
}

resource "google_secret_manager_secret" "sendgrid_api_key" {
  secret_id = "sendgrid-api-key"
  replication {
    auto {}
  }
}

resource "google_secret_manager_secret" "smarty_auth_id" {
  secret_id = "smarty-auth-id"
  replication {
    auto {}
  }
}

resource "google_secret_manager_secret" "smarty_auth_token" {
  secret_id = "smarty-auth-token"
  replication {
    auto {}
  }
}

resource "google_secret_manager_secret" "mixpanel_token" {
  secret_id = "mixpanel-token"
  replication {
    auto {}
  }
}

resource "google_secret_manager_secret" "airtable_auth_token" {
  secret_id = "airtable-auth-token"
  replication {
    auto {}
  }
}
