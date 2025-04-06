CREATE TABLE keilaaja (
    keilaaja_id SERIAL PRIMARY KEY,
    etunimi VARCHAR(50) NOT NULL,
    sukunimi VARCHAR(50) NOT NULL,
    syntymapaiva DATE NOT NULL,
    aktiivijasen BOOLEAN NOT NULL,
    admin BOOLEAN NOT NULL,
    salasana_hash VARCHAR(60)
);

CREATE TABLE keilahalli (
    keilahalli_id SERIAL PRIMARY KEY,
    nimi VARCHAR(100) NOT NULL,
    kaupunki VARCHAR(50) NOT NULL,
    valtio VARCHAR(50) NOT NULL
);

CREATE TABLE kausi (
    kausi_id SERIAL PRIMARY KEY,
    nimi VARCHAR(20) NOT NULL,
    gp_maara INT NOT NULL,
    suunniteltu_gp_maara INT NOT NULL,
    osallistujamaara INT NOT NULL
);

CREATE TABLE gp (
    gp_id SERIAL PRIMARY KEY,
    kausi_id INT NOT NULL REFERENCES kausi(kausi_id),
    keilahalli_id INT NOT NULL REFERENCES keilahalli(keilahalli_id),
    pvm DATE NOT NULL,
    jarjestysnumero INT NOT NULL,
    on_kultainen_gp BOOLEAN NOT NULL
);

CREATE TABLE tulos (
    tulos_id SERIAL PRIMARY KEY,
    gp_id INT NOT NULL REFERENCES gp(gp_id),
    keilaaja_id INT NOT NULL REFERENCES keilaaja(keilaaja_id),
    sarja1 INT,
    sarja2 INT,
    osallistui BOOLEAN NOT NULL
);

CREATE TABLE kultainengp (
    kultainengp_id SERIAL PRIMARY KEY,
    gp_id INT NOT NULL REFERENCES gp(gp_id),
    keilaaja_id INT NOT NULL REFERENCES keilaaja(keilaaja_id),
    lisapisteet DOUBLE PRECISION NOT NULL
);

CREATE TABLE kuppiksenkunkku (
    kuppiksenkunkku_id SERIAL PRIMARY KEY,
    gp_id INT NOT NULL UNIQUE REFERENCES gp(gp_id),
    hallitseva_id INT NOT NULL REFERENCES keilaaja(keilaaja_id),
    haastaja_id INT NOT NULL REFERENCES keilaaja(keilaaja_id),
    vyo_unohtui BOOLEAN NOT NULL,
    hallitseva_paikalla BOOLEAN NOT NULL,
    haastaja_paikalla BOOLEAN NOT NULL
);

CREATE TABLE keilaaja_kausi (
    keilaaja_kausi_id SERIAL PRIMARY KEY,
    keilaaja_id INT NOT NULL REFERENCES keilaaja(keilaaja_id),
    kausi_id INT NOT NULL REFERENCES kausi(kausi_id),
    paras_sarja INT,
    huonoin_sarja INT,
    kauden_pisteet DOUBLE PRECISION,
    voittoja INT,
    osallistumisia INT
);
