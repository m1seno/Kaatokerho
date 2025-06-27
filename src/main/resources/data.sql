-- KEILAAJAT
INSERT INTO keilaaja (etunimi, sukunimi, syntymapaiva, aktiivijasen, admin, kayttajanimi, salasana_hash) VALUES
('Jami', 'Kallio', '1992-04-23', true, false, null, null),
('Miika', 'Nordblad', '1991-05-27', true, true, 'miseno', '$2y$10$qjgNzBIY5NwI0C.RcpjwhOEGvOHEzBfWfqCg7o8zDAKf.buwsIdBC'),
('Henri', 'Verho', '1988-07-16', true, true, 'hverho', '$2a$12$qt2A5YMC4csifjmykEMBR.xxMS9AZKoTOajCrJQkVR1Tu8zy6DwYa'),
('Ossi', 'Oksa', '1988-10-28', true, false, null, null),
('Niko', 'Reunanen', '1988-01-12', true, false, null, null),
('Mika', 'Sola', '1991-05-03', true, false, null, null),
('Frans', 'Hartman', '1992-08-13', true, false, null, null),
('Asko', 'Haaksi', '1986-07-01', true, false, null, null),
('Juho', 'Korpi', '1986-02-10', true, false, null, null),
('Olli', 'Kajava', '1987-11-15', true, false, null, null),
('Kaleva', 'Latvala', '1984-08-05', true, false, null, null),
('Miikka', 'Korventausta', '1990-08-22', true, false, null, null),
('Sakari', 'Leinonen', '1992-07-28', true, false, null, null),
('Iiro', 'Riikonen', '1992-01-11', true, false, null, null),
('Roope', 'Warro', '1988-06-27', true, false, null, null),
('Mikko', 'Mansikkamäki', '1988-07-01', true, false, null, null),
('Tatu', 'Erlin', '1988-07-01', true, false, null, null),
('Joonas', 'Ollonqvist', '1988-10-20', true, false, null, null);

-- KEILAHALLIT
INSERT INTO keilahalli (nimi, kaupunki, valtio) VALUES
('Centrum U7 Gdańsk Śródmieście', 'Gdansk', 'Puola'),
('Raision Keilahalli', 'Raisio', 'Suomi'),
('Kupittaan Keilahalli', 'Turku', 'Suomi'),
('Aninkaisten Keilahalli', 'Turku', 'Suomi');

-- KAUSI
INSERT INTO kausi (nimi, gp_maara, suunniteltu_gp_maara, osallistujamaara) VALUES
('2024-2025', 10, 13, 18);
