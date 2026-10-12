FROM node:22-alpine AS build

WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .

ARG VITE_API_URL=http://localhost:8000/api
ARG VITE_API_USERNAME=
ARG VITE_API_PASSWORD=
ENV VITE_API_URL=${VITE_API_URL}
ENV VITE_API_USERNAME=${VITE_API_USERNAME}
ENV VITE_API_PASSWORD=${VITE_API_PASSWORD}

RUN npm run build

FROM nginx:1.27-alpine AS runtime

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
