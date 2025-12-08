# KOKU

## Schnellübersicht

![](assets/schedule/welcome_dark.png?raw=true)
![](assets/schedule/welcome_light.png?raw=true)

## Kalenderansicht (Monat)

![](assets/schedule/calendar_monthly_dark.png?raw=true)
![](assets/schedule/calendar_monthly_light.png?raw=true)

# Statistik

## Dashboard

![](assets/statistics/stats_dark.png?raw=true)
![](assets/statistics/stats_light.png?raw=true)

# Installation

## docker und docker-compose installieren

https://docs.docker.com/engine/install

https://docs.docker.com/compose/install

## docker-compose.yml anlegen

[docker-compose-yml](docker-compose.yml?raw=true) in einem Ordner speichern

## SSL Zertifikat generieren

Beispiel mit [mkcert](https://github.com/FiloSottile/mkcert)

```
mkcert -key-file ssl/frontend-ssl-key.pem -cert-file ssl/frontend-ssl-cert.pem <hostname>
mkcert -key-file ssl/keycloak-ssl-key.pem -cert-file ssl/keycloak-ssl-cert.pem <hostname>
```

### Export für externe Systeme

Look for the rootCA.pem file in
```
mkcert -CAROOT
```
copy it to a different machine

## Keycloak IDM Einrichten

### Achtung bei Selbstsignierten Zertifikaten (z.B. mkcert)

Alle Java Container müssen dann einen keystore importieren.
Keystore erzeugen:
```
openssl.exe pkcs12 -export -in rootCA.pem -inkey rootCA-key.pem  -out rootCA.p12 -name koku -password pass:koku
```

## Container starten

## Koku öffnen

https://localhost/
