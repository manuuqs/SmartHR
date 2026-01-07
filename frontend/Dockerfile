# ---------- STAGE 1: Build ----------
FROM node:20-alpine AS build

WORKDIR /app

# Copiamos dependencias
COPY package*.json ./
RUN npm install

# Copiamos el resto del proyecto
COPY . .

# Variables de entorno para Vite (se inyectan en build time)
ARG VITE_API_BASE_URL
ARG VITE_ASSISTANT_BASE_URL

ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
ENV VITE_ASSISTANT_BASE_URL=$VITE_ASSISTANT_BASE_URL

# Build de producción
RUN npm run build


# ---------- STAGE 2: Nginx ----------
FROM nginx:alpine

# Eliminamos config por defecto
RUN rm /etc/nginx/conf.d/default.conf

# Copiamos nuestra config
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copiamos el build generado por Vite
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 3000

CMD ["nginx", "-g", "daemon off;"]
