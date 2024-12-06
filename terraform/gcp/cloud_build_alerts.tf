resource "google_logging_metric" "cloud_build_error_logging_metric" {
  name   = "cloud-build-failures/metric"
  filter = <<EOT
resource.type="build"
severity>=ERROR
EOT
  metric_descriptor {
    metric_kind = "DELTA"
    value_type  = "INT64"
  }
}


resource "google_monitoring_alert_policy" "cloud_build_error_alert_policy" {
  count = var.slack_notification_channel != "" ? 1 : 0

  display_name = "cloud-build-error-alert"
  notification_channels = [var.slack_notification_channel]
  conditions {
    display_name = "Build Failure"
    condition_threshold {
      filter     = "metric.type=\"cloud-build-failures/metric\" AND resource.type=\"build\""
      comparison = "COMPARISON_GT"
      threshold_value = 0
      duration = "0s"
      trigger {
        count = 1
      }
    }
  }
  combiner = "AND"
  severity = "ERROR"

  alert_strategy {}

  depends_on = [
    google_logging_metric.cloud_build_error_logging_metric
  ]
}

resource "google_logging_metric" "cloud_build_success_logging_metric" {
  name   = "cloud-build-success/metric"
  filter = <<EOT
resource.type="build"
SEARCH("Finished Step #2 - \"Deploy to k8s\"")
EOT
  metric_descriptor {
    metric_kind = "DELTA"
    value_type  = "INT64"
  }
}

resource "google_monitoring_alert_policy" "cloud_build_success_alert_policy" {
  count = var.slack_notification_channel != "" ? 1 : 0

  display_name = "cloud-build-success-alert"
  notification_channels = [var.slack_notification_channel]
  conditions {
    display_name = "Build Success"
    condition_threshold {
      filter     = "metric.type=\"cloud-build-success/metric\" AND resource.type=\"build\""
      comparison = "COMPARISON_GT"
      threshold_value = 0
      duration = "0s"
      trigger {
        count = 1
      }
    }
  }
  combiner = "AND"
  severity = "INFO"

  alert_strategy {}

  depends_on = [
    google_logging_metric.cloud_build_error_logging_metric
  ]
}
