
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
('Retención en la fuente a titulo de renta'),
('Retención en la fuente a titulo de IVA'),
('Autorretenedor'),
('Gran contribuyente');



insert into country(id,name) VALUE(1,'Colombia');

insert into department(id,country_id,name)VALUE(1,1,'Amazonas');
insert into city (id,department_id,name) VALUES (1,1,'Leticia');
INSERT INTO city (id,department_id,name) VALUES (2,1,'El Encanto');
INSERT INTO city (id,department_id,name) VALUES (3,1,'La Chorrera');
INSERT INTO city (id,department_id,name) VALUES (4,1,'La Pedrera');

INSERT INTO department(id,country_id,name)VALUE(2,1,'Antioquia');
INSERT INTO city (id,department_id,name) VALUES (5,2,'Medellin');
INSERT INTO city (id,department_id,name) VALUES (6,2,'Bello');
INSERT INTO city (id,department_id,name) VALUES (7,2,'Itagui');
INSERT INTO city (id,department_id,name) VALUES (8,2,'Envigado');
INSERT INTO city (id,department_id,name) VALUES (9,2,'Rionegro');

INSERT INTO department(id,country_id,name)VALUE(3,1,'Arauca');
INSERT INTO city (id,department_id,name) VALUES (10,3,'Arauca');

INSERT INTO department(id,country_id,name)VALUE(4,1,'Atlántico');
INSERT INTO city (id,department_id,name) VALUES (11,4,'Barranquilla');
INSERT INTO city (id,department_id,name) VALUES (12,4,'Soledad');
INSERT INTO city (id,department_id,name) VALUES (13,4,'Puerto Colombia');

INSERT INTO department(id,country_id,name)VALUE(5,1,'Bolivar');
INSERT INTO city (id,department_id,name) VALUES (14,5,'Cartagena de Indias');
INSERT INTO city (id,department_id,name) VALUES (15,5,'Magangue');
INSERT INTO city (id,department_id,name) VALUES (16,5,'El carmen de Bolivar');
INSERT INTO city (id,department_id,name) VALUES (17,5,'Mompox');

INSERT INTO department(id,country_id,name)VALUE(6,1,'Boyacá');
INSERT INTO city (id,department_id,name) VALUES (18,6,'Tunja');
INSERT INTO city (id,department_id,name) VALUES (19,6,'Sogamoso');
INSERT INTO city (id,department_id,name) VALUES (20,6,'Duitama');

INSERT INTO department(id,country_id,name)VALUE(7,1,'Caldas');
INSERT INTO city (id,department_id,name) VALUES (21,7,'Manizales');
INSERT INTO city (id,department_id,name) VALUES (22,7,'La Dorada');
INSERT INTO city (id,department_id,name) VALUES (23,7,'Chinchina');
INSERT INTO city (id,department_id,name) VALUES (24,7,'Aguadas');
INSERT INTO city (id,department_id,name) VALUES (25,7,'Filadelfia');

INSERT INTO department(id,country_id,name)VALUE(8,1,'Caquetá');
INSERT INTO city (id,department_id,name) VALUES (26,8,'Florencia');
INSERT INTO city (id,department_id,name) VALUES (27,8,'Morelia');

INSERT INTO department(id,country_id,name)VALUE(9,1,'Casanare');
INSERT INTO city (id,department_id,name) VALUES (28,9,'Yopal');
INSERT INTO city (id,department_id,name) VALUES (29,9,'Aguazul');
INSERT INTO city (id,department_id,name) VALUES (30,9,'Chámeza');
INSERT INTO city (id,department_id,name) VALUES (31,9,'Hato Corozal');
INSERT INTO city (id,department_id,name) VALUES (32,9,'La Salina');

INSERT INTO department(id,country_id,name)VALUE(10,1,'Cauca');
INSERT INTO city (id,department_id,name) VALUES (33,10,'Popayán');
INSERT INTO city (id,department_id,name) VALUES (34,10,'Santander de Quilichao');
INSERT INTO city (id,department_id,name) VALUES (35,10,'Patía');
INSERT INTO city (id,department_id,name) VALUES (36,10,'Puerto Tejada');

INSERT INTO department(id,country_id,name)VALUE(11,1,'Cesar');
INSERT INTO city (id,department_id,name) VALUES (37,11,'Valledupar');
INSERT INTO city (id,department_id,name) VALUES (38,11,'Aguachica');
INSERT INTO city (id,department_id,name) VALUES (39,11,'Aguazul');
INSERT INTO city (id,department_id,name) VALUES (40,11,'Pelaya');
INSERT INTO city (id,department_id,name) VALUES (41,11,'Bosconia');

INSERT INTO department(id,country_id,name)VALUE(12,1,'Choco');
INSERT INTO city (id,department_id,name) VALUES (42,12,'Quibdó');
INSERT INTO city (id,department_id,name) VALUES (43,12,'Acandí');
INSERT INTO city (id,department_id,name) VALUES (44,12,'Alto Baudó');
INSERT INTO city (id,department_id,name) VALUES (45,12,'Atrato');
INSERT INTO city (id,department_id,name) VALUES (46,12,'Bagadó');

INSERT INTO department(id,country_id,name)VALUE(13,1,'Cordoba');
INSERT INTO city (id,department_id,name) VALUES (47,13,'Córdoba');
INSERT INTO city (id,department_id,name) VALUES (48,13,'Puente Genil');
INSERT INTO city (id,department_id,name) VALUES (49,13,'Lucena');
INSERT INTO city (id,department_id,name) VALUES (50,13,'Montilla');
INSERT INTO city (id,department_id,name) VALUES (51,13,'Priego de Córdoba');

INSERT INTO department(id,country_id,name)VALUE(14,1,'Cundinamarca');
INSERT INTO city (id,department_id,name) VALUES (52,14,'Bogotá');
INSERT INTO city (id,department_id,name) VALUES (53,14,'Soacha');
INSERT INTO city (id,department_id,name) VALUES (54,14,'Zipaquirá');
INSERT INTO city (id,department_id,name) VALUES (55,14,'Chía');
INSERT INTO city (id,department_id,name) VALUES (56,14,'Facatativá');

INSERT INTO department(id,country_id,name)VALUE(15,1,'Guainia');
INSERT INTO city (id,department_id,name) VALUES (57,15,'Inírida ');
INSERT INTO city (id,department_id,name) VALUES (58,15,'San Felipe');
INSERT INTO city (id,department_id,name) VALUES (59,15,'Puerto Colombia');
INSERT INTO city (id,department_id,name) VALUES (60,15,'La Guadalupe');
INSERT INTO city (id,department_id,name) VALUES (61,15,'Cacahual');

INSERT INTO department(id,country_id,name)VALUE(16,1,'Guaviare');
INSERT INTO city (id,department_id,name) VALUES (62,16,'San José del Guaviare');
INSERT INTO city (id,department_id,name) VALUES (63,16,'Calamar');
INSERT INTO city (id,department_id,name) VALUES (64,16,'El Retorno');
INSERT INTO city (id,department_id,name) VALUES (65,16,'Miraflores');

INSERT INTO department (id,country_id, name) VALUES (17,1,'Huila');
INSERT INTO department (id,country_id, name) VALUES (18,1,'La Guajira');
INSERT INTO department (id,country_id, name) VALUES (19,1,'Magdalena');
INSERT INTO department (id,country_id, name) VALUES (20,1,'Meta');
INSERT INTO department (id,country_id, name) VALUES (21,1,'Nariño');
INSERT INTO department (id,country_id, name) VALUES (22,1,'Norte de santander');
INSERT INTO department (id,country_id, name) VALUES (23,1,'Putumayo');
INSERT INTO department (id,country_id, name) VALUES (24,1,'Quindio');
INSERT INTO department (id,country_id, name) VALUES (25,1,'Risaralda');
INSERT INTO department (id,country_id, name) VALUES (26,1,'SanAndresyProvidencia');
INSERT INTO department (id,country_id,name) VALUES (27,1,'Santander');
INSERT INTO department (id,country_id, name) VALUES (28,1,'Sucre');
INSERT INTO department (id,country_id, name) VALUES (29,1,'Tolima');
INSERT INTO department (id,country_id, name) VALUES (30,1,'ValledelCauca');
INSERT INTO department (id,country_id, name) VALUES (31,1,'Vaupes');
INSERT INTO department (id,country_id, name) VALUES (32,1,'Vichada');


