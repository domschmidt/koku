# KOKU

![](https://github.com/domschmidt/koku/blob/master/assets/koku_logo.svg?raw=true)

# Übersicht

## Schnellübersicht

![](assets/schedule/welcome.png?raw=true)

## Kalenderansicht (Monat)

![](assets/schedule/calendar_monthly.png?raw=true)

## Kalenderansicht (Woche)

![](assets/schedule/calendar_weekly.png?raw=true)

## Schnelle Terminerfassung

![](assets/schedule/customer_appointment.gif?raw=true)

# Dokumentenmanagement

Schnelle Erstellung von Dokumentenvorlagen

![](assets/document/document_create.gif?raw=true)

Digitale Erfassung der Unterschrift

![](assets/document/document_fill.gif?raw=true)

Finales Ergebnis als PDF

![](assets/document/document_example.png?raw=true)

# Statistik

## Aktuelle Kennzahlen

![](assets/statistics/dashboard.png?raw=true)

## Umsätze

![](assets/statistics/revenues.png?raw=true)

# Installation

## docker und docker-compose installieren

https://docs.docker.com/engine/install

https://docs.docker.com/compose/install

## docker-compose.yml anlegen

[docker-compose-yml](docker-compose.yml?raw=true) in einem Ordner speichern

## SSL Zertifikat erstellen

```
sudo openssl req -x509 -nodes -days 365 -newkey rsa:2048 -keyout ssl/ssl_certificate.key -out ssl/ssl_certificate.crt  
```

## Container starten

```
docker-compose up
```

## Zugriffsrechte für Uploads setzen (Linux Hosts)

```
sudo chown -R $(sudo docker exec koku-backend id -u) .
```

## Koku öffnen

https://localhost/
