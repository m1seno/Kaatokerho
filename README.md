# Kaatokerho backend -Dokumentaatio

## API
#### [Autentikointi](#auth-endpointit)
#### [GP](#gp-endpointit)
#### [Kausi](#kausi-endpointit)
#### [Keilaaja](#keilaaja-endpointit)

Yleistä

	•	GET–pyynnöt: public (nykyisessä configissa)
	•	POST / PATCH / DELETE: vaatii Authorization: Bearer <JWT> ja roolin ROLE_ADMIN
	•	Content-Type: application/json; charset=utf-8

### Auth-endpointit
	•	Malli: stateless JWT (Bearer-token Authorization-headerissa)
	•	Algoritmi: HS512 (salainen avain Base64-enkoodattuna)
	•	Roolit: ROLE_ADMIN / ROLE_USER (sidottu Keilaaja.admin)

#### POST /api/login

Kirjautuu sisään ja palauttaa JWT:n.

#### Request (JSON)
```
{
  "kayttajanimi": "username",
  "salasana": "password"
}
```
#### Responses

•	200 OK
```
{
  "token": "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9..."
}
``````

•	401 Unauthorized (väärä käyttäjätunnus/salasana)

```
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Bad credentials"
}
```
#### Huomiot

    •	Token viedään jatkossa headeriin:
    Authorization: Bearer <JWT>

	•	Token sisältää:
	•	sub = käyttäjänimi
	•	role = ROLE_ADMIN/ROLE_USER
	•	iat, exp

#### cURL
```
curl -sX POST http://localhost:8080/api/login \
 -H "Content-Type: application/json" \
 -d '{"kayttajanimi":"miika","salasana":"salainen"}'
```
#### [🔗 Takaisin valikkoon](#api)

### GP-endpointit

#### GET /api/gp

Palauttaa kaikki GP:t kaikki kausilta.

Response 200
```
[
  {
    "gpId": 1,
    "jarjestysnumero": 1,
    "pvm": "2025-09-21",
    "onKultainenGp": true,
    "keilahalli": { "keilahalliId": 3, "nimi": "Kupittaa" },
    "kausi": { "kausiId": 7, "nimi": "2025-2026" }
  }
]
```

#### GET /api/gp/{id}

Hakee yksittäisen GP:n.

Response 200 – GP löytyi
```
    {
    "gpId": 1,
    "jarjestysnumero": 1,
    "pvm": "2025-09-21",
    "onKultainenGp": true,
    "keilahalli": { "keilahalliId": 3, "nimi": "Kupittaa" },
    "kausi": { "kausiId": 7, "nimi": "2025-2026" }
  }
  ```
- Response 404 – Ei löytynyt

#### GET /api/gp/kausi/{kausiId}

Hakee kaikki annetun kauden GP:t nousevassa järjestyksessä kentän jarjestysnumero mukaan.

Path parameters

    - kausiId (Long, required) – kauden tunniste

Response 200 OK

```
[
  {
    "gpId": 123,
    "jarjestysnumero": 1,
    "pvm": "2025-09-12",
    "onKultainenGp": false,
    "keilahalli": { /* ... */ },
    "kausi": { /* ... */ }
  },
  {
    "gpId": 124,
    "jarjestysnumero": 2,
    "pvm": "2025-09-26",
    "onKultainenGp": true,
    "keilahalli": { /* ... */ },
    "kausi": { /* ... */ }
  }
]
```
#### GET /api/gp/kausi/current

Hakee viimeisimmän kauden kaikki GP:t nousevassa järjestyksessä.

Response 200 OK
```
[
  {
    "gpId": 221,
    "jarjestysnumero": 1,
    "pvm": "2025-10-03",
    "onKultainenGp": false,
    "keilahalli": { /* ... */ },
    "kausi": { /* ... */ }
  }
]
```
Errors
- 400 Not Found ("Ei aktiivista kautta.")

#### POST /api/gp

Luo uuden GP:n.

Request body (UusiGpDTO):
```
{
  "pvm": "2025-10-20",
  "keilahalliId": 3,
  "kultainenGp": true,
  "kausiId": 7
}
```
Säännöt:

	•	Aktiivinen kausi = findTopByOrderByKausiIdDesc()
	•	Jos kauden gpMaara ≥ suunniteltuGpMaara → 400/409 (nyt heitetään
        IllegalStateException, mappaa mahdollisesti controller adviceen)
	•	Jos kultainenGp == true ja kaudella on jo 2 kultaisia → 400/409

Response 201
```
{
  "gpId": 12,
  "jarjestysnumero": 6,
  "pvm": "2025-10-20",
  "onKultainenGp": true,
  "keilahalli": { "keilahalliId": 3, "nimi": "Kupittaa" },
  "kausi": { "kausiId": 7, "nimi": "2025-2026" }
}
```
Virheet:

	•	400 Bad Request – validointivirheet (pvm, keilahalliId, kultainenGp puuttuu/väärä)
	•	404 Not Found – keilahallia ei löytynyt
	•	409 Conflict – kultaisen määräraja/kauden gp-katto (suositeltava status; nyt tulee IllegalStateException)

#### PATCH /api/gp/{id}

Päivittää vain annetut kentät: pvm, keilahalliId, onKultainenGp.

Request body (PaivitaGpDTO) — kaikki vapaaehtoisia:
```
{
  "pvm": "2025-10-27",
  "keilahalliId": 5,
  "onKultainenGp": false
}
```
Rules:

	•	Keilahalli vaihdossa 404 jos ei löydy
	•	Kultaisuusvaihto tarkistetaan service-tasolla (max 2/kausi)

Response 200 – päivitetty GP

404 – GP:tä ei löytynyt

#### DELETE /api/gp/{id}

Poistaa GP:n turvallisesti:

	1.	Poistaa GP:n tulokset
	2.	Poistaa KuppiksenKunkku-rivit (GP:hen sidotut)
	3.	Poistaa kultaisen GP:n merkinnän (no-op jos ei ole)
	4.	Poistaa GP-rivin
	5.	Decrementtaa kausi.gpMaara
	6.	Laskee sarjataulukon uusiksi (kaikki GP:t)

Response 204 No Content

404 – GP:tä ei löytynyt

#### [🔗 Takaisin valikkoon](#api)

### Kausi-endpointit

#### GET /api/kausi
Hakee kaikki tietokantaan tallennetut kaudet.

Vastaus 200 OK
```
[
  {
    "kausiId": 1,
    "nimi": "Kausi 2024–2025",
    "gpMaara": 8,
    "suunniteltuGpMaara": 13,
    "osallistujamaara": 14
  },
  {
    "kausiId": 2,
    "nimi": "Kausi 2025–2026",
    "gpMaara": 0,
    "suunniteltuGpMaara": 13,
    "osallistujamaara": 16
  }
]
```
Vastaus 404 (ei kausia)
```
{
  "status": 404,
  "message": "Yhtään kautta ei ole vielä tallennettu"
}
```
#### GET /api/kausi/current
Kuvaus:
Hakee viimeisimmän kauden (suurin kausiId).

Vastaus 200 OK
```
{
  "kausiId": 3,
  "nimi": "Kausi 2025–2026",
  "gpMaara": 0,
  "suunniteltuGpMaara": 13,
  "osallistujamaara": 18
}
```
Vastaus 404
```
{
  "status": 404,
  "message": "Yhtään kautta ei ole vielä tallennettu"
}
```
#### GET /api/kausi/{id}

Kuvaus:
Hakee yksittäisen kauden tiedot sen id-tunnuksen perusteella.

Parametrit
- id (Long, required) – haettavan kauden tunniste

Vastaus 200 OK
```
{
  "kausiId": 2,
  "nimi": "Kausi 2025–2026",
  "gpMaara": 4,
  "suunniteltuGpMaara": 10,
  "osallistujamaara": 12
}
```
Vastaus 404
```
{
  "status": 404,
  "message": "Kautta ei löytynyt ID:llä 999"
}
```
#### POST /api/kausi

Kuvaus:
Luo uuden kauden. Käyttöoikeus: admin.

Request Body
```
{
  "nimi": "Kausi 2025–2026",
  "suunniteltuGpMaara": 10,
  "osallistujamaara": 14
}
```
Vastaus 201 Created
```
{
  "kausiId": 4,
  "nimi": "Kausi 2025–2026",
  "gpMaara": 0,
  "suunniteltuGpMaara": 10,
  "osallistujamaara": 14
}
```
Vastaus 400 (duplikaatti)
```
{
  "status": 400,
  "message": "Kausi Kausi 2025–2026 on jo olemassa."
}
```
#### PUT /api/kausi/{id}

Kuvaus:
Päivittää olemassa olevan kauden tiedot.
Käyttöoikeus: admin.

Parametrit
- id (Long, required) – päivitettävän kauden tunniste

Request Body
```
{
  "nimi": "Kausi 2025–2026 ",
  "suunniteltuGpMaara": 12,
  "osallistujamaara": 15
}
```
Vastaus 200 OK
```
{
  "kausiId": 4,
  "nimi": "Kausi 2025–2026",
  "gpMaara": 0,
  "suunniteltuGpMaara": 12,
  "osallistujamaara": 15
}
```
Vastaus 400 (nimi jo käytössä)
```
{
  "status": 400,
  "message": "Kausi Kausi 2025–2026 on jo olemassa."
}
```
#### DELETE /api/kausi/{id}

Kuvaus:
Poistaa kauden pysyvästi tietokannasta. Käyttöoikeus: admin.

Vastaus 204 No Content

Vastaus 404
```
{
  "status": 404,
  "message": "Kautta ei löytynyt ID:llä 999"
}
```
#### [🔗 Takaisin valikkoon](#api)

### Keilaaja-endpointit
Base URL: /api/keilaaja

Autentikointi: Bearer JWT

#### GET /api/keilaaja

Hakee kaikki keilaajat.

Response 200
```
[
  {
    "keilaajaId": 1,
    "etunimi": "Matti",
    "sukunimi": "Meikäläinen",
    "syntymapaiva": "1990-04-12",
    "aktiivijasen": true,
    "admin": false,
    "kayttajanimi": MaMe
  }
]
```
#### GET /api/keilaaja/{id}

Hakee keilaajan tunnisteella.

Path param: id (Long)

Response 200
```
{
  "keilaajaId": 1,
  "etunimi": "Matti",
  "sukunimi": "Meikäläinen",
  "syntymapaiva": "1990-04-12",
  "aktiivijasen": true,
  "admin": true,
  "kayttajanimi": "matti"
}
```
Response 404
```
{ "status": 404, "message": "Keilaajaa ei löydy id:llä 999" }
```
#### POST /api/keilaaja  (ADMIN)

Luo uuden keilaajan.

Request
```
{
  "etunimi": "Matti",
  "sukunimi": "Meikäläinen",
  "syntymapaiva": "1990-04-12",
  "aktiivijasen": true,
  "admin": true,
  "kayttajanimi": "matti",
  "salasana": "Salasana123!"
}
```
Response 201
```
{
  "keilaajaId": 10,
  "etunimi": "Matti",
  "sukunimi": "Meikäläinen",
  "syntymapaiva": "1990-04-12",
  "aktiivijasen": true,
  "admin": true,
  "kayttajanimi": "matti"
}
```
Response 400 (duplikaatti käyttäjänimi)
```
{ "status": 400, "message": "Käyttäjänimi matti on jo käytössä." }
```
#### PUT /api/keilaaja/{id}  (ADMIN)

Päivittää keilaajan tiedot (ei sisällä salasanan vaihtoa).

Request
```
{
  "etunimi": "Matti",
  "sukunimi": "Meikäläinen",
  "syntymapaiva": "1990-04-12",
  "aktiivijasen": true,
  "admin": false,
  "kayttajanimi": MaMeik
}
```
Response 200
```
{
  "keilaajaId": 10,
  "etunimi": "Matti",
  "sukunimi": "Meikäläinen",
  "syntymapaiva": "1990-04-12",
  "aktiivijasen": true,
  "admin": false,
  "kayttajanimi": MaMeik
}
```
#### PUT /api/keilaaja/{id}/salasana

Vaihda keilaajan salasana.
Nykylogiikalla vaatii adminin (vaihda tämä!!)

Request
```
{
  "vanhaSalasana": "Salasana123!",
  "uusiSalasana": "UusiVahvaSalasana456!"
}
```
Response 200
```
{ "message": "Salasana päivitetty onnistuneesti!" }
```
Response 400 (väärä vanha)
```
{ "status": 400, "message": "Väärä vanha salasana" }
```
Response 404
```
{ "status": 404, "message": "Keilaajaa ei löytynyt ID:llä 10" }
```
#### DELETE /api/keilaaja/{id}  (ADMIN)

Poistaa keilaajan.

Response 204 (ei sisältöä)

Response 404
```
{ "status": 404, "message": "Keilaajaa ei löytynyt ID:llä 999" }
```
#### [🔗 Takaisin valikkoon](#api)

