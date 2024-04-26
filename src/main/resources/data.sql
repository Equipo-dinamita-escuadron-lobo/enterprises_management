
insert into enterprise_type (name) values
('Privada'),
('Oficial'),
('Mixta');

insert into tax_payer_type(name) values
('Responsable de IVA'),
('No Responsable de IVA'),
('Regimen simple de tributacion'),
('Entidad sin animo de lucro');

insert into tax_liability(name) values
('Información exogena'),
('Facturador electronico'),
('Informante de beneficiarios finales'),
('Retención en la fuente a titulo de renta'),
('Retención en la fuente a titulo de iva'),
('Autorretenedor'),
('Gran contribuyente');



insert into country(id,name) VALUES(1,'Colombia');

insert into department(id,country_id,name)VALUES(1,1,'Amazonas');
insert into city (id,department_id,name) VALUES (1,1,'Leticia');
INSERT INTO city (id,department_id,name) VALUES (2,1,'El Encanto');
INSERT INTO city (id,department_id,name) VALUES (3,1,'La Chorrera');
INSERT INTO city (id,department_id,name) VALUES (4,1,'La Pedrera');

INSERT INTO department(id,country_id,name)VALUES(2,1,'Antioquia');
INSERT INTO city (id,department_id,name) VALUES (5,2,'Medellin');
INSERT INTO city (id,department_id,name) VALUES (6,2,'Bello');
INSERT INTO city (id,department_id,name) VALUES (7,2,'Itagui');
INSERT INTO city (id,department_id,name) VALUES (8,2,'Envigado');
INSERT INTO city (id,department_id,name) VALUES (9,2,'Rionegro');

INSERT INTO department(id,country_id,name)VALUES(3,1,'Arauca');
INSERT INTO city (id,department_id,name) VALUES (10,3,'Arauca');

INSERT INTO department(id,country_id,name)VALUES(4,1,'Atlántico');
INSERT INTO city (id,department_id,name) VALUES (11,4,'Barranquilla');
INSERT INTO city (id,department_id,name) VALUES (12,4,'Soledad');
INSERT INTO city (id,department_id,name) VALUES (13,4,'Puerto Colombia');

INSERT INTO department(id,country_id,name)VALUES(5,1,'Bolivar');
INSERT INTO city (id,department_id,name) VALUES (14,5,'Cartagena de Indias');
INSERT INTO city (id,department_id,name) VALUES (15,5,'Magangue');
INSERT INTO city (id,department_id,name) VALUES (16,5,'El carmen de Bolivar');
INSERT INTO city (id,department_id,name) VALUES (17,5,'Mompox');

INSERT INTO department(id,country_id,name)VALUES(6,1,'Boyacá');
INSERT INTO city (id,department_id,name) VALUES (18,6,'Tunja');
INSERT INTO city (id,department_id,name) VALUES (19,6,'Sogamoso');
INSERT INTO city (id,department_id,name) VALUES (20,6,'Duitama');

INSERT INTO department(id,country_id,name)VALUES(7,1,'Caldas');
INSERT INTO city (id,department_id,name) VALUES (21,7,'Manizales');
INSERT INTO city (id,department_id,name) VALUES (22,7,'La Dorada');
INSERT INTO city (id,department_id,name) VALUES (23,7,'Chinchina');
INSERT INTO city (id,department_id,name) VALUES (24,7,'Aguadas');
INSERT INTO city (id,department_id,name) VALUES (25,7,'Filadelfia');

INSERT INTO department(id,country_id,name)VALUES(8,1,'Caquetá');
INSERT INTO city (id,department_id,name) VALUES (26,8,'Florencia');
INSERT INTO city (id,department_id,name) VALUES (27,8,'Morelia');

INSERT INTO department(id,country_id,name)VALUES(9,1,'Casanare');
INSERT INTO city (id,department_id,name) VALUES (28,9,'Yopal');
INSERT INTO city (id,department_id,name) VALUES (29,9,'Aguazul');
INSERT INTO city (id,department_id,name) VALUES (30,9,'Chámeza');
INSERT INTO city (id,department_id,name) VALUES (31,9,'Hato Corozal');
INSERT INTO city (id,department_id,name) VALUES (32,9,'La Salina');

INSERT INTO department(id,country_id,name)VALUES(10,1,'Cauca');
INSERT INTO city (id,department_id,name) VALUES (33,10,'Popayán');
INSERT INTO city (id,department_id,name) VALUES (34,10,'Santander de Quilichao');
INSERT INTO city (id,department_id,name) VALUES (35,10,'Patía');
INSERT INTO city (id,department_id,name) VALUES (36,10,'Puerto Tejada');

INSERT INTO department(id,country_id,name)VALUES(11,1,'Cesar');
INSERT INTO city (id,department_id,name) VALUES (37,11,'Valledupar');
INSERT INTO city (id,department_id,name) VALUES (38,11,'Aguachica');
INSERT INTO city (id,department_id,name) VALUES (39,11,'Aguazul');
INSERT INTO city (id,department_id,name) VALUES (40,11,'Pelaya');
INSERT INTO city (id,department_id,name) VALUES (41,11,'Bosconia');

INSERT INTO department(id,country_id,name)VALUES(12,1,'Choco');
INSERT INTO city (id,department_id,name) VALUES (42,12,'Quibdó');
INSERT INTO city (id,department_id,name) VALUES (43,12,'Acandí');
INSERT INTO city (id,department_id,name) VALUES (44,12,'Alto Baudó');
INSERT INTO city (id,department_id,name) VALUES (45,12,'Atrato');
INSERT INTO city (id,department_id,name) VALUES (46,12,'Bagadó');

INSERT INTO department(id,country_id,name)VALUES(13,1,'Cordoba');
INSERT INTO city (id,department_id,name) VALUES (47,13,'Córdoba');
INSERT INTO city (id,department_id,name) VALUES (48,13,'Puente Genil');
INSERT INTO city (id,department_id,name) VALUES (49,13,'Lucena');
INSERT INTO city (id,department_id,name) VALUES (50,13,'Montilla');
INSERT INTO city (id,department_id,name) VALUES (51,13,'Priego de Córdoba');

INSERT INTO department(id,country_id,name)VALUES(14,1,'Cundinamarca');
INSERT INTO city (id,department_id,name) VALUES (52,14,'Bogotá');
INSERT INTO city (id,department_id,name) VALUES (53,14,'Soacha');
INSERT INTO city (id,department_id,name) VALUES (54,14,'Zipaquirá');
INSERT INTO city (id,department_id,name) VALUES (55,14,'Chía');
INSERT INTO city (id,department_id,name) VALUES (56,14,'Facatativá');

INSERT INTO department(id,country_id,name)VALUES(15,1,'Guainia');
INSERT INTO city (id,department_id,name) VALUES (57,15,'Inírida ');
INSERT INTO city (id,department_id,name) VALUES (58,15,'San Felipe');
INSERT INTO city (id,department_id,name) VALUES (59,15,'Puerto Colombia');
INSERT INTO city (id,department_id,name) VALUES (60,15,'La Guadalupe');
INSERT INTO city (id,department_id,name) VALUES (61,15,'Cacahual');

INSERT INTO department(id,country_id,name)VALUES(16,1,'Guaviare');
INSERT INTO city (id,department_id,name) VALUES (62,16,'San José del Guaviare');
INSERT INTO city (id,department_id,name) VALUES (63,16,'Calamar');
INSERT INTO city (id,department_id,name) VALUES (64,16,'El Retorno');
INSERT INTO city (id,department_id,name) VALUES (65,16,'Miraflores');


INSERT INTO department (id, country_id, name) VALUES (17, 1, 'Huila');
INSERT INTO city (id, department_id, name) VALUES (66, 17, 'Neiva');
INSERT INTO city (id, department_id, name) VALUES (67, 17, 'Pitalito');
INSERT INTO city (id, department_id, name) VALUES (68, 17, 'Garzon');
INSERT INTO city (id, department_id, name) VALUES (69, 17, 'Campoalegre');
INSERT INTO city (id, department_id, name) VALUES (70, 17, 'La Plata');
INSERT INTO city (id, department_id, name) VALUES (71, 17, 'Gigante');
INSERT INTO city (id, department_id, name) VALUES (72, 17, 'Palermo');
INSERT INTO city (id, department_id, name) VALUES (73, 17, 'Rivera');


INSERT INTO department (id, country_id, name) VALUES (18, 1, 'La Guajira');
INSERT INTO city (id, department_id, name) VALUES (74, 18, 'RioHacha');
INSERT INTO city (id, department_id, name) VALUES (75, 18, 'Maicao');
INSERT INTO city (id, department_id, name) VALUES (76, 18, 'Uribia');
INSERT INTO city (id, department_id, name) VALUES (77, 18, 'Manaure');
INSERT INTO city (id, department_id, name) VALUES (78, 18, 'Dibulla');
INSERT INTO city (id, department_id, name) VALUES (79, 18, 'Barrancas');
INSERT INTO city (id, department_id, name) VALUES (80, 18, 'Fonseca');
INSERT INTO city (id, department_id, name) VALUES (81, 18, 'Albania');


INSERT INTO department (id, country_id, name) VALUES (19, 1, 'Magdalena');
INSERT INTO city (id, department_id, name) VALUES (82, 19, 'Santa Marta');
INSERT INTO city (id, department_id, name) VALUES (83, 19, 'Cienaga');
INSERT INTO city (id, department_id, name) VALUES (84, 19, 'Zona Bananera');
INSERT INTO city (id, department_id, name) VALUES (85, 19, 'Fundacion');
INSERT INTO city (id, department_id, name) VALUES (86, 19, 'El Banco');
INSERT INTO city (id, department_id, name) VALUES (87, 19, 'Plato');
INSERT INTO city (id, department_id, name) VALUES (88, 19, 'Pivijay');
INSERT INTO city (id, department_id, name) VALUES (89, 19, 'Aracataca');


INSERT INTO department (id, country_id, name) VALUES (20, 1, 'Meta');
INSERT INTO city (id, department_id, name) VALUES (90, 20, 'Villavicencio');
INSERT INTO city (id, department_id, name) VALUES (91, 20, 'Acacias');
INSERT INTO city (id, department_id, name) VALUES (92, 20, 'Granada');
INSERT INTO city (id, department_id, name) VALUES (93, 20, 'Puerto Lopez');
INSERT INTO city (id, department_id, name) VALUES (94, 20, 'Restrepo');
INSERT INTO city (id, department_id, name) VALUES (95, 20, 'Cumaral');
INSERT INTO city (id, department_id, name) VALUES (96, 20, 'Puerto Gaitan');
INSERT INTO city (id, department_id, name) VALUES (97, 20, 'San Martin');


INSERT INTO department (id, country_id, name) VALUES (21, 1, 'Nariño');
INSERT INTO city (id, department_id, name) VALUES (98, 21, 'Pasto');
INSERT INTO city (id, department_id, name) VALUES (99, 21, 'Ipiales');
INSERT INTO city (id, department_id, name) VALUES (100, 21, 'Tumaco');
INSERT INTO city (id, department_id, name) VALUES (101, 21, 'La Union');
INSERT INTO city (id, department_id, name) VALUES (102, 21, 'Tuquerres');
INSERT INTO city (id, department_id, name) VALUES (103, 21, 'Samaniego');
INSERT INTO city (id, department_id, name) VALUES (104, 21, 'Barbacoas');
INSERT INTO city (id, department_id, name) VALUES (105, 21, 'El Charco');


INSERT INTO department (id, country_id, name) VALUES (22, 1, 'Norte de santander');
INSERT INTO city (id, department_id, name) VALUES (106, 22, 'San Jose de Cucuta');
INSERT INTO city (id, department_id, name) VALUES (107, 22, 'Ocaña');
INSERT INTO city (id, department_id, name) VALUES (108, 22, 'Pamplona');
INSERT INTO city (id, department_id, name) VALUES (109, 22, 'Villa del Rosario');
INSERT INTO city (id, department_id, name) VALUES (110, 22, 'Los Patios');
INSERT INTO city (id, department_id, name) VALUES (111, 22, 'Tibu');
INSERT INTO city (id, department_id, name) VALUES (112, 22, 'Chinacota');
INSERT INTO city (id, department_id, name) VALUES (113, 22, 'Ragonvalia');


INSERT INTO department (id, country_id, name) VALUES (23, 1, 'Putumayo');
INSERT INTO city (id, department_id, name) VALUES (114, 23, 'Mocoa');
INSERT INTO city (id, department_id, name) VALUES (115, 23, 'Puerto Asis');
INSERT INTO city (id, department_id, name) VALUES (116, 23, 'Orito');
INSERT INTO city (id, department_id, name) VALUES (117, 23, 'Villagarzon');
INSERT INTO city (id, department_id, name) VALUES (118, 23, 'Puerto Guzman');
INSERT INTO city (id, department_id, name) VALUES (119, 23, 'Colon');
INSERT INTO city (id, department_id, name) VALUES (120, 23, 'Santiago');
INSERT INTO city (id, department_id, name) VALUES (121, 23, 'La Hormiga');


INSERT INTO department (id, country_id, name) VALUES (24, 1, 'Quindio');
INSERT INTO city (id, department_id, name) VALUES (122, 24, 'Armenia');
INSERT INTO city (id, department_id, name) VALUES (123, 24, 'Circasia');
INSERT INTO city (id, department_id, name) VALUES (124, 24, 'Calarca');
INSERT INTO city (id, department_id, name) VALUES (125, 24, 'Montenegro');
INSERT INTO city (id, department_id, name) VALUES (126, 24, 'Pijao');
INSERT INTO city (id, department_id, name) VALUES (127, 24, 'Quimbaya');
INSERT INTO city (id, department_id, name) VALUES (128, 24, 'Salento');
INSERT INTO city (id, department_id, name) VALUES (129, 24, 'Buenavista');


INSERT INTO department (id, country_id, name) VALUES (25, 1, 'Risaralda');
INSERT INTO city (id, department_id, name) VALUES (130, 25, 'Pereira');
INSERT INTO city (id, department_id, name) VALUES (131, 25, 'Dosquebradas');
INSERT INTO city (id, department_id, name) VALUES (132, 25, 'La Virginia');
INSERT INTO city (id, department_id, name) VALUES (133, 25, 'Santa Rosa de Cabal');
INSERT INTO city (id, department_id, name) VALUES (134, 25, 'Marsella');
INSERT INTO city (id, department_id, name) VALUES (135, 25, 'Balboa');
INSERT INTO city (id, department_id, name) VALUES (136, 25, 'Santuario');
INSERT INTO city (id, department_id, name) VALUES (137, 25, 'Pueblo Rico');


INSERT INTO department (id, country_id, name) VALUES (26, 1, 'San Andres y Providencia');
INSERT INTO city (id, department_id, name) VALUES (138, 26, 'San Andres');
INSERT INTO city (id, department_id, name) VALUES (139, 26, 'Providencia');


INSERT INTO department (id, country_id, name) VALUES (27, 1, 'Santander');
INSERT INTO city (id, department_id, name) VALUES (140, 27, 'Bucaramanga');
INSERT INTO city (id, department_id, name) VALUES (141, 27, 'Floridablanca');
INSERT INTO city (id, department_id, name) VALUES (142, 27, 'Giron');
INSERT INTO city (id, department_id, name) VALUES (143, 27, 'Barrancabermeja');
INSERT INTO city (id, department_id, name) VALUES (144, 27, 'San Gil');
INSERT INTO city (id, department_id, name) VALUES (145, 27, 'Socorro');
INSERT INTO city (id, department_id, name) VALUES (146, 27, 'Malaga');
INSERT INTO city (id, department_id, name) VALUES (147, 27, 'Barbosa');


INSERT INTO department (id, country_id, name) VALUES (28, 1, 'Sucre');
INSERT INTO city (id, department_id, name) VALUES (148, 28, 'Sincelejo');
INSERT INTO city (id, department_id, name) VALUES (149, 28, 'Corozal');
INSERT INTO city (id, department_id, name) VALUES (150, 28, 'Coveñas');


INSERT INTO department (id, country_id, name) VALUES (29, 1, 'Tolima');
INSERT INTO city (id, department_id, name) VALUES (151, 29, 'Ibague');
INSERT INTO city (id, department_id, name) VALUES (152, 29, 'Espinal');
INSERT INTO city (id, department_id, name) VALUES (153, 29, 'Melgar');
INSERT INTO city (id, department_id, name) VALUES (154, 29, 'Girardot');
INSERT INTO city (id, department_id, name) VALUES (155, 29, 'Chaparral');
INSERT INTO city (id, department_id, name) VALUES (156, 29, 'Libano');
INSERT INTO city (id, department_id, name) VALUES (157, 29, 'Honda');


INSERT INTO department (id, country_id, name) VALUES (30, 1, 'Valle del Cauca');
INSERT INTO city (id, department_id, name) VALUES (158, 30, 'Cali');
INSERT INTO city (id, department_id, name) VALUES (159, 30, 'Buenaventura');
INSERT INTO city (id, department_id, name) VALUES (160, 30, 'Palmira');
INSERT INTO city (id, department_id, name) VALUES (161, 30, 'Jamundi');
INSERT INTO city (id, department_id, name) VALUES (162, 30, 'Tulua');
INSERT INTO city (id, department_id, name) VALUES (163, 30, 'Buga');
INSERT INTO city (id, department_id, name) VALUES (164, 30, 'Cartago');


INSERT INTO department (id, country_id, name) VALUES (31, 1, 'Vaupes');
INSERT INTO city (id, department_id, name) VALUES (165, 31, 'Mitu');
INSERT INTO city (id, department_id, name) VALUES (166, 31, 'Caruru');
INSERT INTO city (id, department_id, name) VALUES (167, 31, 'Taraira');
INSERT INTO city (id, department_id, name) VALUES (168, 31, 'Pacoa');
INSERT INTO city (id, department_id, name) VALUES (169, 31, 'Papunahua');
INSERT INTO city (id, department_id, name) VALUES (170, 31, 'Yavarate');


INSERT INTO department (id, country_id, name) VALUES (32, 1,'Vichada');
INSERT INTO city (id, department_id, name) VALUES (171, 32, 'Puerto Carreño');
INSERT INTO city (id, department_id, name) VALUES (172, 32, 'Cumaribo');
INSERT INTO city (id, department_id, name) VALUES (173, 32, 'La Primavera');
INSERT INTO city (id, department_id, name) VALUES (174, 32, 'Santa Rosalia');
INSERT INTO city (id, department_id, name) VALUES (175, 32, 'Puerto Inirida');




