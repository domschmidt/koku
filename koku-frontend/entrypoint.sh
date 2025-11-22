#!/bin/sh
set -e

# Replace placeholders with environment variables
envsubst < /usr/share/nginx/html/authconfig.template.json > /usr/share/nginx/html/authconfig.json

# Start nginx
exec nginx -g 'daemon off;'
