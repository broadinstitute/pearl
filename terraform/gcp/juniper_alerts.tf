# creates alerts on the juniper-dev namespace
# for application errors.

# create a logging metric to count errors. if you want to
# filter out errors, you can edit the filter.
resource "google_logging_metric" "juniper_app_error_logging_metric" {
  name   = "juniper-error/metric"
  filter = <<EOT
severity>=ERROR AND resource.labels.namespace_name="${var.k8s_namespace}" AND (resource.labels.container_name = "admin" OR resource.labels.container_name = "participant")
EOT
  metric_descriptor {
    metric_kind = "DELTA"
    value_type  = "INT64"
  }
}

# NOTE: ANYTIME YOU EDIT THIS, MAKE SURE YOU DISABLE
#       "notify on incident closure" MANUALLY VIA THE UI
#        There is no way to disable this via terraform
#        and it adds noise to the channel.
resource "google_monitoring_alert_policy" "juniper_app_error_alert_policy" {
  count = var.slack_notification_channel != "" ? 1 : 0

  display_name = "juniper-error-alert"
  notification_channels = [var.slack_notification_channel]
  conditions {
    display_name = "juniper-error-condition"
    condition_threshold {
      filter     = "metric.type=\"logging.googleapis.com/user/juniper-error/metric\" AND resource.type=\"k8s_container\""
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

  alert_strategy {
    notification_prompts = ["OPENED"]
  }

  depends_on = [
    google_logging_metric.juniper_app_error_logging_metric
  ]
}
