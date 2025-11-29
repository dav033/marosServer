-- Crear tabla de compañías
CREATE TABLE IF NOT EXISTS companies (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    address VARCHAR(255),
    type VARCHAR(50) NOT NULL CHECK (type IN ('REGULAR', 'CONTRACTOR')),
    service_id BIGINT,
    is_customer BOOLEAN NOT NULL DEFAULT false
);

-- Crear índices para mejorar el rendimiento
CREATE INDEX IF NOT EXISTS idx_companies_type ON companies(type);
CREATE INDEX IF NOT EXISTS idx_companies_is_customer ON companies(is_customer);
