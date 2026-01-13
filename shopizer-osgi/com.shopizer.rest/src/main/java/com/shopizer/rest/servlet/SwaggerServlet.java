package com.shopizer.rest.servlet;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Swagger UI Servlet
 * Serves the Swagger UI HTML page and OpenAPI specification
 */
public class SwaggerServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/index.html")) {
            // Serve Swagger UI HTML
            serveSwaggerUI(response);
        } else if (pathInfo.equals("/openapi.json")) {
            // Serve OpenAPI specification
            serveOpenAPISpec(response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void serveSwaggerUI(HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        String html = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Shopizer OSGI API Documentation</title>
    <link rel="stylesheet" type="text/css" href="https://unpkg.com/swagger-ui-dist@5.11.0/swagger-ui.css">
    <style>
        html { box-sizing: border-box; overflow: -moz-scrollbars-vertical; overflow-y: scroll; }
        *, *:before, *:after { box-sizing: inherit; }
        body { margin:0; padding:0; }
    </style>
</head>
<body>
    <div id="swagger-ui"></div>
    <script src="https://unpkg.com/swagger-ui-dist@5.11.0/swagger-ui-bundle.js"></script>
    <script src="https://unpkg.com/swagger-ui-dist@5.11.0/swagger-ui-standalone-preset.js"></script>
    <script>
    window.onload = function() {
        window.ui = SwaggerUIBundle({
            url: "/api/docs/openapi.json",
            dom_id: '#swagger-ui',
            deepLinking: true,
            presets: [
                SwaggerUIBundle.presets.apis,
                SwaggerUIStandalonePreset
            ],
            plugins: [
                SwaggerUIBundle.plugins.DownloadUrl
            ],
            layout: "StandaloneLayout"
        });
    };
    </script>
</body>
</html>
                """;

        response.getWriter().write(html);
    }

    private void serveOpenAPISpec(HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        OpenAPI openAPI = createOpenAPISpec();

        // Manually create JSON (simple approach for OSGI)
        String json = generateOpenAPIJson(openAPI);

        response.getWriter().write(json);
    }

    private OpenAPI createOpenAPISpec() {
        OpenAPI openAPI = new OpenAPI();

        // API Info
        Info info = new Info()
            .title("Shopizer OSGI API")
            .description("REST API for Shopizer Component-Based E-Commerce Platform")
            .version("1.0.0")
            .contact(new Contact()
                .name("Shopizer Team")
                .email("support@shopizer.com"))
            .license(new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT"));

        openAPI.info(info);

        // Servers
        List<Server> servers = new ArrayList<>();
        servers.add(new Server()
            .url("http://localhost:8080/api/v1")
            .description("Development server"));
        openAPI.servers(servers);

        return openAPI;
    }

    private String generateOpenAPIJson(OpenAPI openAPI) {
        // Simplified OpenAPI JSON generation
        return """
{
  "openapi": "3.0.3",
  "info": {
    "title": "Shopizer OSGI API",
    "description": "REST API for Shopizer Component-Based E-Commerce Platform",
    "version": "1.0.0",
    "contact": {
      "name": "Shopizer Team",
      "email": "support@shopizer.com"
    },
    "license": {
      "name": "MIT License",
      "url": "https://opensource.org/licenses/MIT"
    }
  },
  "servers": [
    {
      "url": "http://localhost:8080/api/v1",
      "description": "Development server"
    }
  ],
  "tags": [
    { "name": "Catalog", "description": "Product and Category management" },
    { "name": "Cart", "description": "Shopping cart operations" },
    { "name": "Order", "description": "Order processing and payments" },
    { "name": "Customer", "description": "Customer authentication and profiles" },
    { "name": "Merchant", "description": "Store management and analytics" }
  ],
  "paths": {
    "/products": {
      "get": {
        "tags": ["Catalog"],
        "summary": "Get all products",
        "responses": {
          "200": {
            "description": "Successful response",
            "content": {
              "application/json": {
                "schema": {
                  "type": "array",
                  "items": { "$ref": "#/components/schemas/Product" }
                }
              }
            }
          }
        }
      },
      "post": {
        "tags": ["Catalog"],
        "summary": "Create a new product",
        "requestBody": {
          "required": true,
          "content": {
            "application/json": {
              "schema": { "$ref": "#/components/schemas/ProductRequest" }
            }
          }
        },
        "responses": {
          "201": {
            "description": "Product created",
            "content": {
              "application/json": {
                "schema": { "$ref": "#/components/schemas/Product" }
              }
            }
          }
        }
      }
    },
    "/products/{id}": {
      "get": {
        "tags": ["Catalog"],
        "summary": "Get product by ID",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "required": true,
            "schema": { "type": "integer", "format": "int64" }
          }
        ],
        "responses": {
          "200": {
            "description": "Successful response",
            "content": {
              "application/json": {
                "schema": { "$ref": "#/components/schemas/Product" }
              }
            }
          },
          "404": { "description": "Product not found" }
        }
      },
      "put": {
        "tags": ["Catalog"],
        "summary": "Update product",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "required": true,
            "schema": { "type": "integer", "format": "int64" }
          }
        ],
        "requestBody": {
          "required": true,
          "content": {
            "application/json": {
              "schema": { "$ref": "#/components/schemas/ProductRequest" }
            }
          }
        },
        "responses": {
          "200": {
            "description": "Product updated",
            "content": {
              "application/json": {
                "schema": { "$ref": "#/components/schemas/Product" }
              }
            }
          }
        }
      },
      "delete": {
        "tags": ["Catalog"],
        "summary": "Delete product",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "required": true,
            "schema": { "type": "integer", "format": "int64" }
          }
        ],
        "responses": {
          "204": { "description": "Product deleted" }
        }
      }
    },
    "/categories": {
      "get": {
        "tags": ["Catalog"],
        "summary": "Get all categories",
        "responses": {
          "200": {
            "description": "Successful response",
            "content": {
              "application/json": {
                "schema": {
                  "type": "array",
                  "items": { "$ref": "#/components/schemas/Category" }
                }
              }
            }
          }
        }
      }
    },
    "/cart": {
      "get": {
        "tags": ["Cart"],
        "summary": "View cart",
        "parameters": [
          {
            "name": "customerId",
            "in": "query",
            "required": true,
            "schema": { "type": "integer", "format": "int64" }
          }
        ],
        "responses": {
          "200": {
            "description": "Successful response",
            "content": {
              "application/json": {
                "schema": { "$ref": "#/components/schemas/Cart" }
              }
            }
          }
        }
      }
    },
    "/cart/items": {
      "post": {
        "tags": ["Cart"],
        "summary": "Add item to cart",
        "requestBody": {
          "required": true,
          "content": {
            "application/json": {
              "schema": {
                "type": "object",
                "properties": {
                  "customerId": { "type": "integer", "format": "int64" },
                  "productId": { "type": "integer", "format": "int64" },
                  "quantity": { "type": "integer" }
                }
              }
            }
          }
        },
        "responses": {
          "201": {
            "description": "Item added to cart",
            "content": {
              "application/json": {
                "schema": { "$ref": "#/components/schemas/Cart" }
              }
            }
          }
        }
      }
    },
    "/customers/register": {
      "post": {
        "tags": ["Customer"],
        "summary": "Register new customer",
        "requestBody": {
          "required": true,
          "content": {
            "application/json": {
              "schema": {
                "type": "object",
                "properties": {
                  "email": { "type": "string" },
                  "password": { "type": "string" },
                  "firstName": { "type": "string" },
                  "lastName": { "type": "string" }
                }
              }
            }
          }
        },
        "responses": {
          "201": {
            "description": "Customer registered",
            "content": {
              "application/json": {
                "schema": {
                  "type": "object",
                  "properties": {
                    "customer": { "$ref": "#/components/schemas/Customer" },
                    "token": { "type": "string" }
                  }
                }
              }
            }
          }
        }
      }
    },
    "/orders": {
      "post": {
        "tags": ["Order"],
        "summary": "Create new order",
        "requestBody": {
          "required": true,
          "content": {
            "application/json": {
              "schema": {
                "type": "object",
                "properties": {
                  "customerId": { "type": "integer", "format": "int64" },
                  "shippingAddressId": { "type": "integer", "format": "int64" },
                  "paymentMethod": { "type": "string" }
                }
              }
            }
          }
        },
        "responses": {
          "201": {
            "description": "Order created",
            "content": {
              "application/json": {
                "schema": { "$ref": "#/components/schemas/Order" }
              }
            }
          }
        }
      }
    }
  },
  "components": {
    "schemas": {
      "Product": {
        "type": "object",
        "properties": {
          "id": { "type": "integer", "format": "int64" },
          "name": { "type": "string" },
          "description": { "type": "string" },
          "price": { "type": "number", "format": "double" },
          "stockQuantity": { "type": "integer" },
          "sku": { "type": "string" },
          "categoryId": { "type": "integer", "format": "int64" }
        }
      },
      "ProductRequest": {
        "type": "object",
        "required": ["name", "price", "stockQuantity", "sku"],
        "properties": {
          "name": { "type": "string" },
          "description": { "type": "string" },
          "price": { "type": "number", "format": "double" },
          "stockQuantity": { "type": "integer" },
          "sku": { "type": "string" },
          "categoryId": { "type": "integer", "format": "int64" }
        }
      },
      "Category": {
        "type": "object",
        "properties": {
          "id": { "type": "integer", "format": "int64" },
          "name": { "type": "string" },
          "description": { "type": "string" }
        }
      },
      "Cart": {
        "type": "object",
        "properties": {
          "cartId": { "type": "integer", "format": "int64" },
          "customerId": { "type": "integer", "format": "int64" },
          "items": {
            "type": "array",
            "items": { "$ref": "#/components/schemas/CartItem" }
          },
          "totalAmount": { "type": "number", "format": "double" }
        }
      },
      "CartItem": {
        "type": "object",
        "properties": {
          "id": { "type": "integer", "format": "int64" },
          "productId": { "type": "integer", "format": "int64" },
          "quantity": { "type": "integer" },
          "price": { "type": "number", "format": "double" }
        }
      },
      "Customer": {
        "type": "object",
        "properties": {
          "id": { "type": "integer", "format": "int64" },
          "email": { "type": "string" },
          "firstName": { "type": "string" },
          "lastName": { "type": "string" }
        }
      },
      "Order": {
        "type": "object",
        "properties": {
          "orderId": { "type": "integer", "format": "int64" },
          "customerId": { "type": "integer", "format": "int64" },
          "orderNumber": { "type": "string" },
          "status": { "type": "string" },
          "totalAmount": { "type": "number", "format": "double" }
        }
      }
    }
  }
}
                """;
    }
}
