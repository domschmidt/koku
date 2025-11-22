# ---- Build Angular App ----
FROM node:22 as build

WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build --production

# ---- Run with NGINX ----
FROM nginx:stable-alpine

# Remove default nginx website
RUN rm -rf /usr/share/nginx/html/*

# Copy built Angular dist (adjust folder name if needed)
COPY --from=build /app/dist/koku/browser /usr/share/nginx/html/

# Copy custom nginx config
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy template for dynamic authconfig
COPY authconfig.template.json /usr/share/nginx/html/authconfig.template.json

# Copy entrypoint script
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

EXPOSE 80

# Use entrypoint to generate authconfig.json dynamically
CMD ["/entrypoint.sh"]
