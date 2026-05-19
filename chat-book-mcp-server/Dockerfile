FROM node:24-alpine AS build

WORKDIR /app

COPY package*.json ./
RUN npm ci --no-audit --fund=false

COPY tsconfig.json ./
COPY src ./src
RUN npm run build
RUN npm prune --omit=dev

FROM node:24-alpine

LABEL maintainer="chat-book-cloud"

ENV NODE_ENV=production
ENV TZ=Asia/Shanghai

RUN apk add --no-cache tzdata \
    && cp /usr/share/zoneinfo/$TZ /etc/localtime \
    && echo $TZ > /etc/timezone

WORKDIR /app

COPY --from=build /app/package*.json ./
COPY --from=build /app/node_modules ./node_modules
COPY --from=build /app/dist ./dist

EXPOSE 3001

CMD ["node", "dist/server.js"]
