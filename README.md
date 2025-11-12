# Kaatokerho backend -Dokumentaatio

## API
#### [Autentikointi](#autentikointi)
#### [GP](#gp)

### Autentikointi
	•	Malli: stateless JWT (Bearer-token Authorization-headerissa)
	•	Algoritmi: HS512 (salainen avain Base64-enkoodattuna)
	•	Roolit: ROLE_ADMIN / ROLE_USER (sidottu Keilaaja.admin)

#### Endpointit
*POST /api/login*

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

### GP
Yleistä

	•	Base URL: /api/gp
	•	Auth:
	•	GET–pyynnöt: public (nykyisessä configissa)
	•	POST / PATCH / DELETE: vaatii Authorization: Bearer <JWT> ja roolin ROLE_ADMIN
	•	Content-Type: application/json; charset=utf-8

#### Endpointit
##### GET /api/gp

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

##### GET /api/gp/{id}

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

##### GET /api/gp/kausi/{kausiId}

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
##### GET /api/gp/kausi/current

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

##### POST /api/gp

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

##### PATCH /api/gp/{id}

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

##### DELETE /api/gp/{id}

Poistaa GP:n turvallisesti:

	1.	Poistaa GP:n tulokset
	2.	Poistaa KuppiksenKunkku-rivit (GP:hen sidotut)
	3.	Poistaa kultaisen GP:n merkinnän (no-op jos ei ole)
	4.	Poistaa GP-rivin
	5.	Decrementtaa kausi.gpMaara
	6.	Laskee sarjataulukon uusiksi (kaikki GP:t)

Response 204 No Content

404 – GP:tä ei löytynyt

