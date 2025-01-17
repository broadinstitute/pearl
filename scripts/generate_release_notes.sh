#!/bin/sh

# Fetch the latest Juniper tags, this doesn't happen automatically
git fetch --tags

# Check what versions demo and production are currently running
demoResponse=$(curl -s 'https://juniper-cmi.dev/version')
demoGitTag=$(echo $demoResponse | jq -r '.gitTag')

prodResponse=$(curl -s 'https://juniper-cmi.org/version')
prodGitTag=$(echo $prodResponse | jq -r '.gitTag')

echo "Demo is currently running git tag \033[32m$demoGitTag\033[0m"
echo "Production is currently running git tag \033[32m$prodGitTag\033[0m"
echo "Generating release notes...\n"

# Pull the differing commits between the demo and production tags
# git log JSON formatter adapted from https://gist.github.com/textarcana/1306223
commits=$(git log $demoGitTag...$prodGitTag \
    --pretty=format:'{%n "message": "%s"%n},' \
    $@ | \
    perl -pe 'BEGIN{print "["}; END{print "]\n"}' | \
    perl -pe 's/},]/}]/')

messages=$(echo $commits | jq -r '.[].message')

# Generate the release notes markdown. This can be pasted directly into a Jira ticket
# or a Google Doc (if you have markdown enabled)
while IFS= read -r message; do
  if echo "$message" | grep -q -E 'JN-\d+'; then
    ticket=$(echo "$message" | grep -o -E 'JN-\d+')
    pr=$(echo "$message" | grep -o -E '#\d+' | tr -d '#')
    message=$(echo "$message" | tr -d '[]')
    echo "[${message}](https://broadworkbench.atlassian.net/browse/$ticket) [(GitHub PR)](https://github.com/broadinstitute/juniper/pull/$pr)"
  fi
done <<< "$messages"
