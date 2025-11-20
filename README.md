# Kaatokerho backend -Dokumentaatio

## API
#### [Autentikointi](#auth-endpointit)
#### [GP](#gp-endpointit)
#### [Kausi](#kausi-endpointit)
#### [Keilaaja](#keilaaja-endpointit)
#### [KeilaajaKausi](#keilaajakausi-endpointit)
#### [Keilahalli](#keilahalli-endpointit)
#### [Kultainen GP](#kultainengp-endpointit)
#### [Kuppiksen Kunkku](#kuppiksenkunkku-endpointit)
#### [Tulos](#tulos-endpointit)
#### [Sarjataulukko](#sarjataulukko-endpointit)
#### [Kalenteri](#kalenteri-endpointit)


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

### KeilaajaKausi-endpointit
#### GET /api/keilaajakausi

Hakee kaikki keilaajakausi-instanssit kaikkien kausien ja keilaajien osalta.

Vastaus 200 OK
```
[
  {
    "keilaajaKausiId": 12,
    "keilaajaId": 3,
    "keilaajaNimi": "Matti Meikäläinen",
    "kausiId": 1,
    "kausiNimi": "Kausi 2025",
    "parasSarja": 221,
    "huonoinSarja": 135,
    "kaudenPisteet": 45.0,
    "voittoja": 1,
    "osallistumisia": 3
  }
]
```
#### GET /api/keilaajakausi/kausi/{kausiId}

Hakee tietyn kauden kaikkien keilaajien tilastot.
Käytetään esimerkiksi sarjataulukon näyttämiseen.

Parametrit
- kausiId – long

Vastaus 200 OK

Lista ResponseKeilaajaKausiDTO-olioita.

#### GET /api/keilaajakausi/keilaaja/{keilaajaId}

Hakee keilaajan kaikki kausitilastot.

Parametrit
- keilaajaId – long

Vastaus 200 OK

Lista keilaajan kausitilastoista.
GET /api/keilaajakausi/keilaaja/{keilaajaId}/kausi/{kausiId}

Hakee yhden keilaajan tilaston yhdeltä kaudelta.

Parametrit
	•	keilaajaId
	•	kausiId

Vastaus 200 OK
#### GET /api/keilaajakausi/keilaaja/{keilaajaId}/kausi/{kausiId}

Hakee yhden keilaajan tilaston yhdeltä kaudelta.

Parametrit
- keilaajaId
- kausiId

Vastaus 200 OK
```
{
  "keilaajaKausiId": 12,
  "keilaajaId": 3,
  "keilaajaNimi": "Matti Meikäläinen",
  "kausiId": 1,
  "kausiNimi": "Kausi 2025",
  "parasSarja": 221,
  "huonoinSarja": 135,
  "kaudenPisteet": 45.0,
  "voittoja": 1,
  "osallistumisia": 3
}
```
#### [🔗 Takaisin valikkoon](#api)

### Keilahalli-endpointit
#### GET /api/keilahalli

Hakee listan kaikista keilahalleista.

Vastaus 200 OK
```
[
  {
    "keilahalliId": 1,
    "nimi": "Raision Keilahalli",
    "kaupunki": "Raisio",
    "valtio": "Suomi"
  },
  {
    "keilahalliId": 2,
    "nimi": "Kupittaan Keilahalli",
    "kaupunki": "Turku",
    "valtio": "Suomi"
  }
]
```
Jos halleja ei ole, palauttaa tyhjän listan [].
#### GET /api/keilahalli/{id}

Hakee yksittäisen keilahallin tiedot id:n perusteella.

Polkuparametrit
- id – keilahalliId (Long)

Onnistunut vastaus 200 OK
```
{
  "keilahalliId": 1,
  "nimi": "Raision Keilahalli",
  "kaupunki": "Raisio",
  "valtio": "Suomi"
}
```
Virhevastaus
	•	404 NOT_FOUND – jos hallia ei löydy
(Heitetään ApiException(HttpStatus.NOT_FOUND, "Keilahallia ei löydy id:llä X"))

#### POST /api/keilahalli

Luo uuden keilahallin.

Request body (UusiKeilahalliDTO)
```
{
  "nimi": "Raision Keilahalli",
  "kaupunki": "Raisio",
  "valtio": "Suomi"
}
```
Validointi:
- nimi – @NotEmpty
- kaupunki – @NotEmpty
- valtio – @NotEmpty

Virheellisestä syötteestä palautuu 400 Bad Request Bean Validation -virheillä.

Onnistunut vastaus 201 CREATED
```
{
  "keilahalliId": 3,
  "nimi": "Raision Keilahalli",
  "kaupunki": "Raisio",
  "valtio": "Suomi"
}
```
#### PUT /api/keilahalli/{id}

Päivittää olemassa olevan keilahallin kaikki kentät (nimi, kaupunki, valtio).

Polkuparametrit
- id – päivitettävän hallin id

Request body (UusiKeilahalliDTO)
```
{
  "nimi": "Uusi Nimi",
  "kaupunki": "Uusi Kaupunki",
  "valtio": "Suomi"
}
```
Onnistunut vastaus 200 OK
```
{
  "keilahalliId": 1,
  "nimi": "Uusi Nimi",
  "kaupunki": "Uusi Kaupunki",
  "valtio": "Suomi"
}
```
Virhevastaus
	•	404 NOT_FOUND – jos hallia ei löydy

#### DELETE /api/keilahalli/{id}

Poistaa keilahallin.

Polkuparametrit
- id – poistettavan hallin id

Onnistunut vastaus 204 NO CONTENT
Ei response bodya.

Virhevastaus
	•	404 NOT_FOUND – jos hallia ei löydy

#### [🔗 Takaisin valikkoon](#api)

### KultainenGP-endpointit

#### GET /api/kultainengp

Hakee kaikki KultainenGp-instanssit järjestelmästä.

Vastaus 200 OK
```
[
  {
    "kultainenGpId": 1,
    "keilaajaId": 12,
    "keilaajaNimi": "Matti Meikäläinen",
    "gpId": 5,
    "gpJarjestysnumero": 3,
    "kausiId": 7,
    "kausiNimi": "2025–2026",
    "lisapisteet": 2.0
  }
]
```
Jos rivejä ei ole, palauttaa tyhjän listan [].

#### GET /api/kultainengp/gp/{gpId}

Hakee tietyn GP:n kaikki KultainenGp-rivit.

Polkuparametrit
- gpId – GP:n id

Onnistunut vastaus 200 OK
```
[
  {
    "kultainenGpId": 1,
    "keilaajaId": 12,
    "keilaajaNimi": "Matti Meikäläinen",
    "gpId": 5,
    "gpJarjestysnumero": 3,
    "kausiId": 7,
    "kausiNimi": "2025–2026",
    "lisapisteet": 2.0
  },
  {
    "kultainenGpId": 1,
    "keilaajaId": 4,
    "keilaajaNimi": "Kalle Keilaaja",
    "gpId": 5,
    "gpJarjestysnumero": 3,
    "kausiId": 7,
    "kausiNimi": "2025–2026",
    "lisapisteet": -1.0
  }
]
```

Virhevastaus
- 404 NOT_FOUND – jos:
- GP:tä ei löydy ("Gp:tä ei löytynyt id:llä X") tai
- Kultaisia pisteitä ei ole tälle GP:lle ("Tilastoja ei löydy GP:n id:llä X")

#### GET /api/kultainengp/kausi/{kausiId}

Hakee kauden kaikki kultaiset pisteet (kaikki GP:t ja keilaajat kyseisessä kaudessa).

Polkuparametrit
-	kausiId – kauden id

Onnistunut vastaus 200 OK
```
[
  {
    "kultainenGpId": 1,
    "keilaajaId": 12,
    "keilaajaNimi": "Matti Meikäläinen",
    "gpId": 5,
    "gpJarjestysnumero": 3,
    "kausiId": 7,
    "kausiNimi": "2025–2026",
    "lisapisteet": 2.0
  },
  {
    "kultainenGpId": 1,
    "keilaajaId": 4,
    "keilaajaNimi": "Kalle Keilaaja",
    "gpId": 5,
    "gpJarjestysnumero": 3,
    "kausiId": 7,
    "kausiNimi": "2025–2026",
    "lisapisteet": -1.0
  }
]
```

GET /api/kultainengp/keilaaja/{keilaajaId}

Hakee keilaajan kaikki Kultainen GP -pisteet kaikilta kausilta

Parametrit:
	•	keilaajaId

Onnistunut vastaus 200 OK:
```
[
  {
    "kultainenGpId": 12,
    "keilaajaId": 4,
    "keilaajaNimi": "Pekka Pouta",
    "gpId": 20,
    "gpJarjestysnumero": 4,
    "kausiId": 8,
    "kausiNimi": "2024–2025",
    "lisapisteet": 2.0
  }
]
```
#### GET /api/kultainengp/keilaaja/{keilaajaId}/kausi/{kausiId}

Hakee keilaajan Kultainen GP -suoritukset tietyltä kaudelta

Parametrit:
	•	keilaajaId
	•	kausiId

Vastaus (200 OK):
```
[
  {
    "kultainenGpId": 1,
    "keilaajaId": 4,
    "keilaajaNimi": "Kalle Keilaaja",
    "gpId": 5,
    "gpJarjestysnumero": 3,
    "kausiId": 7,
    "kausiNimi": "2025–2026",
    "lisapisteet": 2.0
  },
  {
    "kultainenGpId": 1,
    "keilaajaId": 4,
    "keilaajaNimi": "Kalle Keilaaja",
    "gpId": 14,
    "gpJarjestysnumero": 12,
    "kausiId": 7,
    "kausiNimi": "2025–2026",
    "lisapisteet": -1.0
  }
]
```
#### [🔗 Takaisin valikkoon](#api)

### KuppiksenKunkku-endpointit

KuppiksenKunkkuDTO
```
{
  "id": 123,
  "gpId": 55,
  "gpNo": 3,
  "pvm": "2025-01-02",
  "puolustajaId": 4,
  "puolustajaNimi": "Matti Meikäläinen",
  "haastajaId": 8,
  "haastajaNimi": "Kalle Keilaaja",
  "voittajaId": 8,
  "voittajaNimi": "Kalle Keilaaja",
  "vyoUnohtui": false
}
```
KuppiksenKunkkuStatsDTO
```
{
  "season": "2024–2025",
  "gpCount": 12,
  "currentChampionId": 4,
  "currentChampionName": "Matti Meikäläinen",
  "uniqueChampions": 6,
  "totalChallenges": 12
}
```

Virheiden käsittely (päivitetty ApiException)

Kaikki endpointit palauttavat ApiException-tapauksissa seuraavan rakenteen:

#### GET /api/kk/history?season=KAUSI_NIMI

Hakee kaiken Kuppiksen Kunkku -historian annettulta kaudelta aikajärjestyksessä.

Polkuparametrit:
- Parametri: kauden nimi (esim: "2025-2026")
- Tyyppi = string 

Vastaus 200 OK

Lista KuppiksenKunkkuDTO-olioita järjestettynä GP-numeroittain.

Virheet
- 404: “Kuppiksen Kunkkua ei löytynyt kaudelta X”

#### GET /api/kk/current?season=KAUSI_NIMI

Hakee nykyisen kunkun annetulta kaudelta (kauden viimeisin merkintä).

Vastaus 200 OK

KuppiksenKunkkuDTO

Virheet
- 404: Ei löydy yhtään KK-merkintää kaudelta

#### GET /api/kk/haastajalista/latest

Haastajalista kuvaa seuraavan GP:n haastajajärjestyksen, joka muodostetaan automaattisesti, kun edellisen GP:n tulokset on syötetty ja KuppiksenKunkkuService.kasitteleKuppiksenKunkku(...) on ajettu.

Lista:
- perustuu edellisen GP:n tuloksiin
- ei sisällä puolustajaa – vain haastajat
- jokaisella haastajalla on myös:
	- sarja1 = parempi sarja edellisestä GP:stä
	- sarja2 = huonompi sarja edellisestä GP:stä

Haastajalista pidetään muistissa palvelimen ajon aikana in-memory mapissa haastajalistaByGp, eikä sitä tallenneta tietokantaan.

Vastaus 200 OK:
```
{
  "gpId": 42,
  "gpNo": 7,
  "pvm": "2025-03-15",
  "haastajat": [
    {
      "keilaajaId": 5,
      "nimi": "Kalle Kaataja",
      "sarja1": 201,
      "sarja2": 143
    },
    {
      "keilaajaId": 8,
      "nimi": "Pasi Paikkaaja",
      "sarja1": 189,
      "sarja2": 163
    }
  ]
}
```

Vastaus 404 Not Found:
- haastajalistaa ei ole vielä muodostettu yhdellekään GP:lle
- GP:tä ei löydy ID:llä (jos data on päässyt epäkonsistenttiin tilaan)

#### GET /api/kk/gp/{gpId}

Hakee yksittäisen GP:n Kuppiksen Kunkku -merkinnän.

Polkuparametrit:
- Parametri: gpId
- Tyyppi: Long

Vastaus 200 OK

KuppiksenKunkkuDTO

Virheet
- 404: Jos GP:lle ei ole KK-merkintää

#### GET /api/kk/player/{keilaajaId}

Hakee pelaajakohtaisen historian kaikilta kausilta tai valitulta kaudelta.

Polkuparametrit:
- Parametri: keilaajaId, kauden nimi (Jos annetaan → rajataan vain kyseiseen kauteen
)
- Long, String

Vastaus 200 OK

KuppiksenKunkkuDTO

Virheet
- 404: Pelaajalla ei ole merkintöjä

#### GET /api/kk/stats?season=KAUSI_NIMI

Hakee kauden tilastot:
- GP-määrä
- Nykyinen mestari
- Uniikkien mestareiden määrä
- Haasteiden määrä

Vastaus 200 OK

KuppiksenKunkkuStatsDTO

Virheet
	•	404: Jos kauden historia on tyhjä

### Tulos-endpointit

TulosResponseDTO
```
{
  "tulosId": 123,
  "gpId": 55,
  "keilaajaId": 7,
  "keilaajaEtunimi": "Matti",
  "keilaajaSukunimi": "Meikäläinen",
  "sarja1": 201,
  "sarja2": 143,
  "osallistui": true
}
```
LisaaTuloksetDTO
```
{
  "gpId": 55,
  "vyoUnohtui": false,
  "tulokset": [
    {
      "keilaajaId": 7,
      "sarja1": 201,
      "sarja2": 143
    },
    {
      "keilaajaId": 12,
      "sarja1": 180,
      "sarja2": 150
    }
  ]
}
```
Virheiden käsittely (ApiException)

Kaikki Tulos-endpointit palauttavat virheet muodossa:
```
{
  "status": 404,
  "error": "Not Found",
  "message": "Tuloksia ei löytynyt GP:lle 55",
  "timestamp": "2025-01-01T12:00:00"
}
```

#### POST /api/tulokset

Lisää kaikki yhden GP:n tulokset kerralla.
Idempotentti:
- tallentaa gp:n tulokset
- päivittää KuppiksenKunkku-ketjun ja Kultainen GP:n
- päivittää KeilaajaKausi-tilastot

Request body

(LisaaTuloksetDTO)

Vastaus 200 OK

Lista TulosResponseDTO-olioita.

Virheet
- 404: GP:tä ei löytynyt
- 404: Keilaajaa ei löytynyt
- 400: DTO-validoinnit epäonnistuivat
- 409: Täydellinen tasapeli Kuppiksen kunkku-ottelussa → UI:n pitää valita voittaja

#### GET /api/tulokset/gp/{gpId}

Hakee kaikki tulokset yhdelle GP:lle.

Vastaus 200 OK

Lista TulosResponseDTO-olioita.

Virheet
- 404: GP:tä ei löytynyt
- 404: GP:llä ei ole tuloksia

#### GET /api/tulokset/keilaaja/{keilaajaId}

Hakee keilaajan kaikki tulokset kaikilta GP:iltä.

Vastaus 200 OK

Lista TulosResponseDTO-olioita.

Virheet
- 404: Keilaajaa ei löydy
- 404: Keilaajalla ei ole yhtään tulosta

#### GET /api/tulokset/keilaaja/{keilaajaId}/kausi/{kausiId}

Hakee keilaajan tulokset vain yhdeltä kaudelta.

Vastaus 200 OK

Lista TulosResponseDTO-olioita.

Virheet
- 404: Keilaajaa ei löydy
- 404: Tältä kaudelta ei löydy tuloksia

#### DELETE /api/tulokset/gp/{gpId}

Poistaa kaikki tietyn GP:n tulokset ja:
1.	Poistaa GP:hen liittyvät Kultainen GP -merkinnät
2.	Poistaa GP:hen liittyvät Kuppiksen Kunkku -merkinnät
3.	Uudelleenrakentaa kyseisen kauden KuppiksenKunkku-ketjun
4.	Uudelleenlaskee kauden KeilaajaKausi-tilastot (sarjataulukko)


Vastaus 204 No Content

Virheet
- 404: GP:tä ei löytynyt

#### [🔗 Takaisin valikkoon](#api)

