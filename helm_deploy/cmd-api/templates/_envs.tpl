    {{/* vim: set filetype=mustache: */}}
{{/*
Environment variables for web and worker containers
*/}}
{{- define "deployment.envs" }}
env:
  - name: SPRING_PROFILES_ACTIVE
    value: "postgres"

  - name: SERVER_PORT
    value: "{{ .Values.image.port }}"

  - name: JAVA_OPTS
    value: "{{ .Values.env.JAVA_OPTS }}"

  - name: CSR_ENDPOINT_URL
    value: "{{ .Values.env.CSR_ENDPOINT_URL }}"

  - name: SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI
    value: "{{ .Values.env.SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI }}"

  - name: ELITE2API_ENDPOINT_URL
    value: "{{ .Values.env.ELITE2API_ENDPOINT_URL }}"

  - name: DATABASE_USERNAME
    valueFrom:
      secretKeyRef:
        name: check-my-diary-rds
        key: database_username

  - name: DATABASE_PASSWORD
    valueFrom:
      secretKeyRef:
        name: check-my-diary-rds
        key: database_password

  - name: DATABASE_NAME
    valueFrom:
      secretKeyRef:
        name: check-my-diary-rds
        key: database_name

  - name: DATABASE_ENDPOINT
    valueFrom:
      secretKeyRef:
        name: check-my-diary-rds
        key: rds_instance_endpoint

  - name: APPLICATION_NOTIFY_KEY
    valueFrom:
        secretKeyRef:
          name: check-my-diary-notifications
          key: NOTIFY_CLIENT_KEY

  - name: APPINSIGHTS_INSTRUMENTATIONKEY
    valueFrom:
      secretKeyRef:
        name: {{ template "app.name" . }}
        key: APPINSIGHTS_INSTRUMENTATIONKEY

  - name: OAUTH_CLIENT_ID
    valueFrom:
      secretKeyRef:
         name: check-my-diary
         key: API_CLIENT_ID

  - name: OAUTH_CLIENT_SECRET
    valueFrom:
      secretKeyRef:
         name: check-my-diary
         key: API_CLIENT_SECRET

  - name: OAUTH_ENDPOINT_URL
    value: "{{ .Values.env.OAUTH_ROOT_URL }}"
{{- end -}}
