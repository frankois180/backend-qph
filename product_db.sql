/******************************
-  Nombre:      1.- Creación de base de datos
-  Descripción: Definición y creación del contenedor principal
               para el sistema de gestión de productos.
-  Tipo:        Estructura (Database)
-  Versión:     1.0
-  Autor:       Stalin Ladino
-  Fecha:       13-04-2026
*******************************/
CREATE DATABASE products_db;


/******************************
-  Nombre:      2.- Creación de tabla de productos
-  Descripción: Definición y creación de la tabla principal
               que almacena el catálogo de productos con
               control de precios y stock disponible.
-  Tipo:        Estructura (Table)
-  Versión:     1.0
-  Autor:       Stalin Ladino
-  Fecha:       13-04-2026
*******************************/
CREATE TABLE public.products (
    id      serial         NOT NULL,
    nombre  varchar(255)   NOT NULL,
    precio  numeric(38, 2) NOT NULL,
    stock   int4           NOT NULL,
    CONSTRAINT products_pkey       PRIMARY KEY (id),
    CONSTRAINT products_stock_check CHECK ((stock >= 0))
);


/******************************
-  Nombre:      3.- Usuarios, ventas y detalle
-  Descripción: Usuarios con rol, cabecera de venta y
               líneas que descuentan el stock del producto.
-  Tipo:        Estructura (Table)
*******************************/
CREATE TABLE public.users (
    id       serial       NOT NULL,
    username varchar(100) NOT NULL,
    password varchar(255) NOT NULL,
    role     varchar(20)  NOT NULL,
    CONSTRAINT users_pkey PRIMARY KEY (id),
    CONSTRAINT users_username_uk UNIQUE (username),
    CONSTRAINT users_role_check CHECK (role IN ('ADMIN', 'VENDEDOR'))
);

CREATE TABLE public.sales (
    id         serial         NOT NULL,
    user_id    int4           NOT NULL,
    total      numeric(38, 2) NOT NULL,
    created_at timestamptz    NOT NULL DEFAULT now(),
    CONSTRAINT sales_pkey PRIMARY KEY (id),
    CONSTRAINT sales_user_fk FOREIGN KEY (user_id) REFERENCES public.users (id)
);

CREATE INDEX sales_user_id_idx ON public.sales (user_id);

CREATE TABLE public.sale_items (
    id         serial         NOT NULL,
    sale_id    int4           NOT NULL,
    product_id int4           NOT NULL,
    quantity   int4           NOT NULL,
    unit_price numeric(38, 2) NOT NULL,
    line_total numeric(38, 2) NOT NULL,
    CONSTRAINT sale_items_pkey PRIMARY KEY (id),
    CONSTRAINT sale_items_sale_fk FOREIGN KEY (sale_id) REFERENCES public.sales (id),
    CONSTRAINT sale_items_product_fk FOREIGN KEY (product_id) REFERENCES public.products (id),
    CONSTRAINT sale_items_quantity_check CHECK (quantity > 0)
);

CREATE INDEX sale_items_sale_id_idx ON public.sale_items (sale_id);

-- Usuario local de arranque. La contraseña es admin123 (BCrypt).
-- El token JWT no vive en el código: se solicita en POST /api/auth/login.
INSERT INTO public.users (username, password, role)
SELECT 'admin',
       '$2a$10$eMSPwJfxqXRuTXFmPRKy2.CvXAMpOQ1q3Wgvc4cOkli9RuOBK5Cge',
       'ADMIN'
WHERE NOT EXISTS (
    SELECT 1 FROM public.users WHERE username = 'admin'
);