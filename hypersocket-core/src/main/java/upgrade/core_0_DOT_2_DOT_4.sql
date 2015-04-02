EXIT IF FRESH
INSERT INTO ssl_certificates SELECT * FROM certificates;
DROP TABLE certificates;
