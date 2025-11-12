# Kaatokerho backend -Dokumentaatio

## API

### Autentikointi
	•	Malli: stateless JWT (Bearer-token Authorization-headerissa)
	•	Algoritmi: HS512 (salainen avain Base64-enkoodattuna)
	•	Roolit: ROLE_ADMIN / ROLE_USER (sidottu Keilaaja.admin)

#### Endpointit
POST /api/login

Kirjautuu sisään ja palauttaa JWT:n.

##### Request (JSON)
```
{
  "kayttajanimi": "username",
  "salasana": "password"
}
```
##### Responses

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
##### Huomiot

    •	Token viedään jatkossa headeriin:
    Authorization: Bearer <JWT>

	•	Token sisältää:
	•	sub = käyttäjänimi
	•	role = ROLE_ADMIN/ROLE_USER
	•	iat, exp

##### cURL
```
curl -sX POST http://localhost:8080/api/login \
 -H "Content-Type: application/json" \
 -d '{"kayttajanimi":"miika","salasana":"salainen"}'
```

