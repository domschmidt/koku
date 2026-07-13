# ---- Build Angular App ----
FROM node:24 AS build

WORKDIR /app

COPY package*.json ./
RUN npm ci --ignore-scripts --legacy-peer-deps

COPY . .
RUN npm run build -- --configuration production

# ---- Run with NGINX ----
FROM nginx:stable-alpine

# Remove default nginx website
RUN rm -rf /usr/share/nginx/html/*

# Copy built Angular dist (adjust folder name if needed)
COPY --from=build /app/dist/koku/browser /usr/share/nginx/html/

# Copy custom nginx config
ARG NGINX_CONFIG=nginx.conf
COPY ${NGINX_CONFIG} /etc/nginx/conf.d/default.conf

# Copy template for dynamic authconfig
COPY authconfig.template.json /usr/share/nginx/html/authconfig.template.json

# Copy entrypoint script
COPY entrypoint.sh /entrypoint.sh
RUN sed -i 's/\r$//' /entrypoint.sh \
    && sed -i '/^user[[:space:]]/d; s#^pid .*#pid /tmp/nginx.pid;#' /etc/nginx/nginx.conf \
    && chmod +x /entrypoint.sh \
    && chown -R nginx:nginx /entrypoint.sh /usr/share/nginx/html /var/cache/nginx /var/log/nginx /var/run

USER nginx

EXPOSE 8080 8443

# Use entrypoint to generate authconfig.json dynamically
CMD ["/entrypoint.sh"]
