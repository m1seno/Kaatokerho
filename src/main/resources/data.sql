-- KEILAAJAT
INSERT INTO keilaaja (etunimi, sukunimi, syntymapaiva, aktiivijasen, admin, salasana_hash) VALUES
('Jami', 'Kallio', '1992-04-23', true, false, null),
('Miika', 'Nordblad', '1991-05-27', true, true, '$2a$12$7yGZ64HFqJ7Hi5I.INkOpeLhYuQklnmUnf5gOHLHlS2FZx1f1Ldye'),
('Henri', 'Verho', '1988-07-16', true, true, '$2a$12$qt2A5YMC4csifjmykEMBR.xxMS9AZKoTOajCrJQkVR1Tu8zy6DwYa'),
('Ossi', 'Oksa', '1988-10-28', true, false, null),
('Niko', 'Reunanen', '1988-01-12', true, false, null),
('Mika', 'Sola', '1991-05-03', true, false, null),
('Frans', 'Hartman', '1992-08-13', true, false, null),
('Asko', 'Haaksi', '1986-07-01', true, false, null),
('Juho', 'Korpi', '1986-02-10', true, false, null),
('Olli', 'Kajava', '1987-11-15', true, false, null),
('Kaleva', 'Latvala', '1984-08-05', true, false, null),
('Miikka', 'Korventausta', '1990-08-22', true, false, null),
('Sakari', 'Leinonen', '1992-07-28', true, false, null),
('Iiro', 'Riikonen', '1992-01-11', true, false, null),
('Roope', 'Warro', '1988-06-27', true, false, null),
('Mikko', 'Mansikkamäki', '1988-07-01', true, false, null),
('Tatu', 'Erlin', '1988-07-01', true, false, null),
('Joonas', 'Ollonqvist', '1988-10-20', true, false, null);

-- KEILAHALLIT
INSERT INTO keilahalli (nimi, kaupunki, valtio) VALUES
('Centrum U7 Gdańsk Śródmieście', 'Gdansk', 'Puola'),
('Raision Keilahalli', 'Raisio', 'Suomi'),
('Kupittaan Keilahalli', 'Turku', 'Suomi'),
('Aninkaisten Keilahalli', 'Turku', 'Suomi');

-- KAUSI
INSERT INTO kausi (nimi, gp_maara, suunniteltu_gp_maara, osallistujamaara) VALUES
('2024-2025', 10, 13, 18);
