
# =====================
# BUILD STAGE (Node)
# =====================
FROM node:20-alpine AS build
WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .
# Si necesitas variables Vite en build, puedes declararlas aquí con ENV
# ENV VITE_API_BASE_URL=http://localhost:8080
# ENV VITE_ASSISTANT_BASE_URL=http://localhost:9090

RUN npm run build

# =====================
# RUNTIME STAGE (Nginx)
# =====================
FROM nginx:1.25-alpine
# Copia el resultado del build
COPY --from=build /app/dist /usr/share/nginx/html

# Cambia el puerto a 3000 en el default.conf
RUN sed -i 's/listen       80;/listen       3000;/' /etc/nginx/conf.d/default.conf

EXPOSE 3000
CMD ["nginx", "-g", "daemon off;"]
