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
    "/customers": {
      "get": {
        "tags": ["Customer"],
        "summary": "Get all customers (Admin)",
        "responses": {
          "200": {
            "description": "List of customers",
            "content": {
              "application/json": {
                "schema": {
                  "type": "array",
                  "items": { "$ref": "#/components/schemas/CustomerResponse" }
                }
              }
            }
          }
        }
      },
      "post": {
        "tags": ["Customer"],
        "summary": "Create a customer (Admin)",
        "requestBody": {
          "required": true,
          "content": {
            "application/json": {
              "schema": { "$ref": "#/components/schemas/CustomerRegistrationRequest" }
            }
          }
        },
        "responses": {
          "201": {
            "description": "Customer created",
            "content": {
              "application/json": {
                "schema": { "$ref": "#/components/schemas/CustomerResponse" }
              }
            }
          }
        }
      }
    },
    "/customers/{id}": {
      "get": {
        "tags": ["Customer"],
        "summary": "Get customer by ID",
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
            "description": "Customer found",
            "content": {
              "application/json": {
                "schema": { "$ref": "#/components/schemas/CustomerResponse" }
              }
            }
          },
          "404": { "description": "Customer not found" }
        }
      },
      "put": {
        "tags": ["Customer"],
        "summary": "Update customer (Admin)",
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
              "schema": { "$ref": "#/components/schemas/CustomerUpdateRequest" }
            }
          }
        },
        "responses": {
          "200": {
            "description": "Customer updated",
            "content": {
              "application/json": {
                "schema": { "$ref": "#/components/schemas/CustomerResponse" }
              }
            }
          },
          "404": { "description": "Customer not found" }
        }
      },
      "delete": {
        "tags": ["Customer"],
        "summary": "Delete customer (Admin)",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "required": true,
            "schema": { "type": "integer", "format": "int64" }
          }
        ],
        "responses": {
          "204": { "description": "Customer deleted" },
          "404": { "description": "Customer not found" }
        }
      }
    },
    "/customers/register": {
      "post": {
        "tags": ["Customer"],
        "summary": "Register a new customer account",
        "description": "FR-024: The system shall allow customers to register new account. Validates email format, password strength, and checks for duplicate accounts.",
        "requestBody": {
          "required": true,
          "content": {
            "application/json": {
              "schema": { "$ref": "#/components/schemas/CustomerRegistrationRequest" }
            }
          }
        },
        "responses": {
          "201": {
            "description": "Customer registered successfully",
            "content": {
              "application/json": {
                "schema": { "$ref": "#/components/schemas/CustomerResponse" }
              }
            }
          },
          "400": { "description": "Bad request (email already exists or invalid data)" }
        }
      }
    },
    "/customers/login": {
      "post": {
        "tags": ["Customer"],
        "summary": "Customer login",
        "description": "FR-025: The system shall allow customers to login. Returns JWT access token and refresh token upon successful authentication.",
        "requestBody": {
          "required": true,
          "content": {
            "application/json": {
              "schema": { "$ref": "#/components/schemas/CustomerLoginRequest" }
            }
          }
        },
        "responses": {
          "200": {
            "description": "Login successful",
            "content": {
              "application/json": {
                "schema": { "$ref": "#/components/schemas/CustomerLoginResponse" }
              }
            }
          },
          "400": { "description": "Invalid credentials" }
        }
      }
    },
    "/customers/{customerId}/logout": {
      "post": {
        "tags": ["Customer"],
        "summary": "Customer logout",
        "description": "FR-025: The system shall allow customers to logout. Client should discard tokens after this call.",
        "parameters": [
          {
            "name": "customerId",
            "in": "path",
            "required": true,
            "schema": { "type": "integer", "format": "int64" }
          }
        ],
        "responses": {
          "200": {
            "description": "Logout successful",
            "content": {
              "application/json": {
                "schema": {
                  "type": "object",
                  "properties": {
                    "message": { "type": "string" }
                  }
                }
              }
            }
          },
          "404": { "description": "Customer not found" }
        }
      }
    },
    "/customers/email/{email}": {
      "get": {
        "tags": ["Customer"],
        "summary": "Get customer by email",
        "parameters": [
          {
            "name": "email",
            "in": "path",
            "required": true,
            "schema": { "type": "string", "format": "email" }
          }
        ],
        "responses": {
          "200": {
            "description": "Customer found",
            "content": {
              "application/json": {
                "schema": { "$ref": "#/components/schemas/Customer" }
              }
            }
          },
          "404": { "description": "Customer not found" }
        }
      }
    },
    "/customers/{customerId}/profile": {
      "get": {
        "tags": ["Customer"],
        "summary": "Get customer profile",
        "description": "FR-026: The system shall allow customers to view their profile. Returns customer details including name, email, phone, and account status.",
        "parameters": [
          {
            "name": "customerId",
            "in": "path",
            "required": true,
            "schema": { "type": "integer", "format": "int64" }
          }
        ],
        "responses": {
          "200": {
            "description": "Customer profile",
            "content": {
              "application/json": {
                "schema": { "$ref": "#/components/schemas/CustomerResponse" }
              }
            }
          },
          "404": { "description": "Customer not found" }
        }
      },
      "put": {
        "tags": ["Customer"],
        "summary": "Update customer profile",
        "description": "FR-026: The system shall allow customers to update their profile. Allows updating first name, last name, and phone number.",
        "parameters": [
          {
            "name": "customerId",
            "in": "path",
            "required": true,
            "schema": { "type": "integer", "format": "int64" }
          }
        ],
        "requestBody": {
          "required": true,
          "content": {
            "application/json": {
              "schema": { "$ref": "#/components/schemas/CustomerProfileUpdateRequest" }
            }
          }
        },
        "responses": {
          "200": {
            "description": "Profile updated",
            "content": {
              "application/json": {
                "schema": { "$ref": "#/components/schemas/CustomerResponse" }
              }
            }
          },
          "404": { "description": "Customer not found" }
        }
      }
    },
    "/customers/{id}/change-password": {
      "post": {
        "tags": ["Customer"],
        "summary": "Change customer password",
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
              "schema": { "$ref": "#/components/schemas/PasswordChangeRequest" }
            }
          }
        },
        "responses": {
          "200": {
            "description": "Password changed successfully",
            "content": {
              "application/json": {
                "schema": {
                  "type": "object",
                  "properties": {
                    "message": { "type": "string" }
                  }
                }
              }
            }
          },
          "400": { "description": "Invalid current password" },
          "404": { "description": "Customer not found" }
        }
      }
    },
    "/customers/{id}/addresses": {
      "get": {
        "tags": ["Customer"],
        "summary": "Get all addresses for customer",
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
            "description": "List of addresses",
            "content": {
              "application/json": {
                "schema": {
                  "type": "array",
                  "items": { "$ref": "#/components/schemas/Address" }
                }
              }
            }
          },
          "404": { "description": "Customer not found" }
        }
      },
      "post": {
        "tags": ["Customer"],
        "summary": "Add new address",
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
              "schema": { "$ref": "#/components/schemas/AddressRequest" }
            }
          }
        },
        "responses": {
          "201": {
            "description": "Address created",
            "content": {
              "application/json": {
                "schema": { "$ref": "#/components/schemas/Address" }
              }
            }
          },
          "404": { "description": "Customer not found" }
        }
      }
    },
    "/customers/{id}/addresses/{addressId}": {
      "get": {
        "tags": ["Customer"],
        "summary": "Get address by ID",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "required": true,
            "schema": { "type": "integer", "format": "int64" }
          },
          {
            "name": "addressId",
            "in": "path",
            "required": true,
            "schema": { "type": "integer", "format": "int64" }
          }
        ],
        "responses": {
          "200": {
            "description": "Address found",
            "content": {
              "application/json": {
                "schema": { "$ref": "#/components/schemas/Address" }
              }
            }
          },
          "404": { "description": "Address not found" }
        }
      },
      "put": {
        "tags": ["Customer"],
        "summary": "Update address",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "required": true,
            "schema": { "type": "integer", "format": "int64" }
          },
          {
            "name": "addressId",
            "in": "path",
            "required": true,
            "schema": { "type": "integer", "format": "int64" }
          }
        ],
        "requestBody": {
          "required": true,
          "content": {
            "application/json": {
              "schema": { "$ref": "#/components/schemas/AddressRequest" }
            }
          }
        },
        "responses": {
          "200": {
            "description": "Address updated",
            "content": {
              "application/json": {
                "schema": { "$ref": "#/components/schemas/Address" }
              }
            }
          },
          "404": { "description": "Address not found" }
        }
      },
      "delete": {
        "tags": ["Customer"],
        "summary": "Delete address",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "required": true,
            "schema": { "type": "integer", "format": "int64" }
          },
          {
            "name": "addressId",
            "in": "path",
            "required": true,
            "schema": { "type": "integer", "format": "int64" }
          }
        ],
        "responses": {
          "204": { "description": "Address deleted" },
          "404": { "description": "Address not found" }
        }
      }
    },
    "/customers/{id}/addresses/{addressId}/default": {
      "put": {
        "tags": ["Customer"],
        "summary": "Set default address",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "required": true,
            "schema": { "type": "integer", "format": "int64" }
          },
          {
            "name": "addressId",
            "in": "path",
            "required": true,
            "schema": { "type": "integer", "format": "int64" }
          }
        ],
        "responses": {
          "200": {
            "description": "Default address set",
            "content": {
              "application/json": {
                "schema": {
                  "type": "object",
                  "properties": {
                    "message": { "type": "string" }
                  }
                }
              }
            }
          },
          "404": { "description": "Address not found" }
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
    },
    "/merchants/register": {
      "post": {
        "tags": ["Merchant"],
        "summary": "Register a new merchant account",
        "description": "FR-015: The system shall allow merchants to register new account. Validates email, password, and checks for duplicate accounts.",
        "requestBody": {
          "required": true,
          "content": {
            "application/json": {
              "schema": { "$ref": "#/components/schemas/MerchantRegistrationRequest" }
            }
          }
        },
        "responses": {
          "201": {
            "description": "Merchant registered successfully",
            "content": {
              "application/json": {
                "schema": { "$ref": "#/components/schemas/MerchantProfileResponse" }
              }
            }
          },
          "400": { "description": "Bad request (email already exists or invalid data)" }
        }
      }
    },
    "/merchants/login": {
      "post": {
        "tags": ["Merchant"],
        "summary": "Merchant login",
        "description": "FR-015: The system shall allow merchants to login. Returns JWT access token upon successful authentication.",
        "requestBody": {
          "required": true,
          "content": {
            "application/json": {
              "schema": { "$ref": "#/components/schemas/LoginRequest" }
            }
          }
        },
        "responses": {
          "200": {
            "description": "Login successful",
            "content": {
              "application/json": {
                "schema": { "$ref": "#/components/schemas/AuthResponse" }
              }
            }
          },
          "401": { "description": "Invalid credentials" }
        }
      }
    },
    "/merchants/stores": {
      "post": {
        "tags": ["Merchant"],
        "summary": "Create a new merchant store",
        "description": "FR-016: The system shall allow merchants to manage store details (Create)",
        "requestBody": {
          "required": true,
          "content": {
            "application/json": {
              "schema": { "$ref": "#/components/schemas/MerchantStoreRequest" }
            }
          }
        },
        "responses": {
          "201": {
            "description": "Store created successfully",
            "content": {
              "application/json": {
                "schema": { "$ref": "#/components/schemas/MerchantStoreResponse" }
              }
            }
          },
          "400": { "description": "Bad request (merchant already has a store)" }
        }
      }
    },
    "/merchants/stores/{storeId}": {
      "get": {
        "tags": ["Merchant"],
        "summary": "Get store details by ID",
        "description": "FR-016: The system shall allow merchants to manage store details (View)",
        "parameters": [
          {
            "name": "storeId",
            "in": "path",
            "required": true,
            "schema": { "type": "integer", "format": "int64" }
          }
        ],
        "responses": {
          "200": {
            "description": "Store details",
            "content": {
              "application/json": {
                "schema": { "$ref": "#/components/schemas/MerchantStoreResponse" }
              }
            }
          },
          "404": { "description": "Store not found" }
        }
      },
      "put": {
        "tags": ["Merchant"],
        "summary": "Update store details",
        "description": "FR-016: The system shall allow merchants to manage store details (Update)",
        "parameters": [
          {
            "name": "storeId",
            "in": "path",
            "required": true,
            "schema": { "type": "integer", "format": "int64" }
          }
        ],
        "requestBody": {
          "required": true,
          "content": {
            "application/json": {
              "schema": { "$ref": "#/components/schemas/MerchantStoreRequest" }
            }
          }
        },
        "responses": {
          "200": {
            "description": "Store updated successfully",
            "content": {
              "application/json": {
                "schema": { "$ref": "#/components/schemas/MerchantStoreResponse" }
              }
            }
          },
          "404": { "description": "Store not found" }
        }
      }
    },
    "/merchants/{merchantId}/stores": {
      "get": {
        "tags": ["Merchant"],
        "summary": "List all stores for a merchant",
        "description": "FR-016: The system shall allow merchants to manage store details (View)",
        "parameters": [
          {
            "name": "merchantId",
            "in": "path",
            "required": true,
            "schema": { "type": "integer", "format": "int64" }
          }
        ],
        "responses": {
          "200": {
            "description": "List of stores",
            "content": {
              "application/json": {
                "schema": {
                  "type": "array",
                  "items": { "$ref": "#/components/schemas/MerchantStoreResponse" }
                }
              }
            }
          }
        }
      }
    },
    "/merchants/{merchantId}/stores/{storeId}": {
      "delete": {
        "tags": ["Merchant"],
        "summary": "Delete a store",
        "description": "FR-016: The system shall allow merchants to manage store details (Delete)",
        "parameters": [
          {
            "name": "merchantId",
            "in": "path",
            "required": true,
            "schema": { "type": "integer", "format": "int64" }
          },
          {
            "name": "storeId",
            "in": "path",
            "required": true,
            "schema": { "type": "integer", "format": "int64" }
          }
        ],
        "responses": {
          "200": { "description": "Store deleted successfully" },
          "404": { "description": "Store not found" }
        }
      }
    },
    "/merchants/{merchantId}/stores/{storeId}/products": {
      "post": {
        "tags": ["Merchant - Inventory"],
        "summary": "Create a product under a store",
        "description": "FR-017: The system shall allow merchants to manage inventory (Create)",
        "operationId": "createProduct",
        "parameters": [
          {
            "name": "merchantId",
            "in": "path",
            "required": true,
            "schema": { "type": "integer", "format": "int64" }
          },
          {
            "name": "storeId",
            "in": "path",
            "required": true,
            "schema": { "type": "integer", "format": "int64" }
          }
        ],
        "requestBody": {
          "required": true,
          "content": {
            "application/json": {
              "schema": { "$ref": "#/components/schemas/MerchantProductCreateRequest" }
            }
          }
        },
        "responses": {
          "201": {
            "description": "Product created successfully",
            "content": {
              "application/json": {
                "schema": { "$ref": "#/components/schemas/InventoryItemResponse" }
              }
            }
          }
        }
      }
    },
    "/merchants/{merchantId}/inventory": {
      "get": {
        "tags": ["Merchant - Inventory"],
        "summary": "Get inventory for a merchant (all stores)",
        "description": "FR-017: The system shall allow merchants to manage inventory (Read)",
        "operationId": "getInventory",
        "parameters": [
          {
            "name": "merchantId",
            "in": "path",
            "required": true,
            "schema": { "type": "integer", "format": "int64" }
          }
        ],
        "responses": {
          "200": {
            "description": "List of inventory items",
            "content": {
              "application/json": {
                "schema": {
                  "type": "array",
                  "items": { "$ref": "#/components/schemas/InventoryItemResponse" }
                }
              }
            }
          }
        }
      }
    },
    "/merchants/{merchantId}/stores/{storeId}/inventory": {
      "get": {
        "tags": ["Merchant - Inventory"],
        "summary": "Get inventory for a specific store",
        "description": "FR-017: The system shall allow merchants to manage inventory (Read)",
        "operationId": "getInventoryByStore",
        "parameters": [
          {
            "name": "merchantId",
            "in": "path",
            "required": true,
            "schema": { "type": "integer", "format": "int64" }
          },
          {
            "name": "storeId",
            "in": "path",
            "required": true,
            "schema": { "type": "integer", "format": "int64" }
          }
        ],
        "responses": {
          "200": {
            "description": "List of inventory items for the store",
            "content": {
              "application/json": {
                "schema": {
                  "type": "array",
                  "items": { "$ref": "#/components/schemas/InventoryItemResponse" }
                }
              }
            }
          }
        }
      }
    },
    "/merchants/{merchantId}/inventory/products/{productId}": {
      "put": {
        "tags": ["Merchant - Inventory"],
        "summary": "Update product details",
        "description": "FR-017: The system shall allow merchants to manage inventory (Update)",
        "operationId": "updateProduct",
        "parameters": [
          {
            "name": "merchantId",
            "in": "path",
            "required": true,
            "schema": { "type": "integer", "format": "int64" }
          },
          {
            "name": "productId",
            "in": "path",
            "required": true,
            "schema": { "type": "integer", "format": "int64" }
          }
        ],
        "requestBody": {
          "required": true,
          "content": {
            "application/json": {
              "schema": { "$ref": "#/components/schemas/InventoryUpdateRequest" }
            }
          }
        },
        "responses": {
          "200": {
            "description": "Product updated successfully",
            "content": {
              "application/json": {
                "schema": { "$ref": "#/components/schemas/InventoryItemResponse" }
              }
            }
          }
        }
      },
      "delete": {
        "tags": ["Merchant - Inventory"],
        "summary": "Delete a product",
        "description": "FR-017: The system shall allow merchants to manage inventory (Delete)",
        "operationId": "deleteProduct",
        "parameters": [
          {
            "name": "merchantId",
            "in": "path",
            "required": true,
            "schema": { "type": "integer", "format": "int64" }
          },
          {
            "name": "productId",
            "in": "path",
            "required": true,
            "schema": { "type": "integer", "format": "int64" }
          }
        ],
        "responses": {
          "204": { "description": "Product deleted successfully" }
        }
      }
    },
    "/merchants/{merchantId}/inventory/low-stock": {
      "get": {
        "tags": ["Merchant - Inventory"],
        "summary": "Get low stock products",
        "description": "FR-018: Identify products needing restock",
        "operationId": "getLowStockProducts",
        "parameters": [
          {
            "name": "merchantId",
            "in": "path",
            "required": true,
            "schema": { "type": "integer", "format": "int64" }
          },
          {
            "name": "storeId",
            "in": "query",
            "required": false,
            "schema": { "type": "integer", "format": "int64" }
          }
        ],
        "responses": {
          "200": {
            "description": "List of low stock products",
            "content": {
              "application/json": {
                "schema": {
                  "type": "array",
                  "items": { "$ref": "#/components/schemas/InventoryItemResponse" }
                }
              }
            }
          }
        }
      }
    }
    ,
    "/merchants/{merchantId}/reports/sales": {
      "get": {
        "tags": ["Merchant"],
        "summary": "Get sales report for a merchant",
        "parameters": [
          { "name": "merchantId", "in": "path", "required": true, "schema": { "type": "integer", "format": "int64" } },
          { "name": "storeId", "in": "query", "required": false, "schema": { "type": "integer", "format": "int64" } },
          { "name": "startDate", "in": "query", "required": true, "schema": { "type": "string", "format": "date" } },
          { "name": "endDate", "in": "query", "required": true, "schema": { "type": "string", "format": "date" } }
        ],
        "responses": {
          "200": {
            "description": "Sales report",
            "content": {
              "application/json": { "schema": { "$ref": "#/components/schemas/SalesReportResponse" } }
            }
          }
        }
      }
    },
    "/merchants/{merchantId}/reports/products": {
      "get": {
        "tags": ["Merchant"],
        "summary": "Get product sales report",
        "parameters": [
          { "name": "merchantId", "in": "path", "required": true, "schema": { "type": "integer", "format": "int64" } },
          { "name": "storeId", "in": "query", "required": false, "schema": { "type": "integer", "format": "int64" } },
          { "name": "categoryId", "in": "query", "required": false, "schema": { "type": "integer", "format": "int64" } },
          { "name": "productId", "in": "query", "required": false, "schema": { "type": "integer", "format": "int64" } },
          { "name": "startDate", "in": "query", "required": false, "schema": { "type": "string", "format": "date" } },
          { "name": "endDate", "in": "query", "required": false, "schema": { "type": "string", "format": "date" } }
        ],
        "responses": {
          "200": {
            "description": "Product sales report",
            "content": {
              "application/json": {
                "schema": { "type": "array", "items": { "$ref": "#/components/schemas/ProductReportResponse" } }
              }
            }
          }
        }
      }
    },
    "/merchants/{merchantId}/reports/products/{productId}/analytics": {
      "get": {
        "tags": ["Merchant"],
        "summary": "Get product analytics",
        "parameters": [
          { "name": "merchantId", "in": "path", "required": true, "schema": { "type": "integer", "format": "int64" } },
          { "name": "productId", "in": "path", "required": true, "schema": { "type": "integer", "format": "int64" } },
          { "name": "startDate", "in": "query", "required": false, "schema": { "type": "string", "format": "date" } },
          { "name": "endDate", "in": "query", "required": false, "schema": { "type": "string", "format": "date" } }
        ],
        "responses": {
          "200": {
            "description": "Product analytics",
            "content": {
              "application/json": { "schema": { "$ref": "#/components/schemas/ProductAnalyticsResponse" } }
            }
          }
        }
      }
    },
    "/merchants/{merchantId}/products/{productId}/views": {
      "post": {
        "tags": ["Merchant"],
        "summary": "Record product view",
        "parameters": [
          { "name": "merchantId", "in": "path", "required": true, "schema": { "type": "integer", "format": "int64" } },
          { "name": "productId", "in": "path", "required": true, "schema": { "type": "integer", "format": "int64" } }
        ],
        "responses": {
          "200": { "description": "Product view recorded" }
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
          "passwordHash": { "type": "string" },
          "firstName": { "type": "string" },
          "lastName": { "type": "string" },
          "phone": { "type": "string" },
          "status": { "type": "string" },
          "emailVerified": { "type": "boolean" },
          "lastLoginAt": { "type": "string", "format": "date-time" },
          "createdAt": { "type": "string", "format": "date-time" },
          "updatedAt": { "type": "string", "format": "date-time" }
        }
      },
      "CustomerResponse": {
        "type": "object",
        "properties": {
          "id": { "type": "integer", "format": "int64" },
          "email": { "type": "string" },
          "firstName": { "type": "string" },
          "lastName": { "type": "string" },
          "phone": { "type": "string" },
          "emailVerified": { "type": "boolean" },
          "status": { "type": "string" },
          "lastLoginAt": { "type": "string", "format": "date-time" },
          "createdAt": { "type": "string", "format": "date-time" }
        }
      },
      "CustomerRegistrationRequest": {
        "type": "object",
        "required": ["email", "password", "confirmPassword", "firstName", "lastName"],
        "properties": {
          "email": { "type": "string" },
          "password": { "type": "string" },
          "confirmPassword": { "type": "string" },
          "firstName": { "type": "string" },
          "lastName": { "type": "string" },
          "phone": { "type": "string" }
        }
      },
      "CustomerLoginRequest": {
        "type": "object",
        "required": ["email", "password"],
        "properties": {
          "email": { "type": "string" },
          "password": { "type": "string" }
        }
      },
      "CustomerLoginResponse": {
        "type": "object",
        "properties": {
          "accessToken": { "type": "string" },
          "refreshToken": { "type": "string" },
          "tokenType": { "type": "string" },
          "expiresIn": { "type": "integer", "format": "int64" },
          "customer": { "$ref": "#/components/schemas/CustomerResponse" }
        }
      },
      "CustomerProfileUpdateRequest": {
        "type": "object",
        "properties": {
          "firstName": { "type": "string" },
          "lastName": { "type": "string" },
          "phone": { "type": "string" }
        }
      },
      "CustomerUpdateRequest": {
        "type": "object",
        "properties": {
          "firstName": { "type": "string" },
          "lastName": { "type": "string" },
          "phone": { "type": "string" },
          "email": { "type": "string" },
          "status": { "type": "string" }
        }
      },
      "CustomerAuthResponse": {
        "type": "object",
        "properties": {
          "id": { "type": "integer", "format": "int64" },
          "email": { "type": "string", "format": "email" },
          "firstName": { "type": "string" },
          "lastName": { "type": "string" },
          "phoneNumber": { "type": "string" },
          "token": { "type": "string" },
          "tokenType": { "type": "string", "default": "Bearer" }
        }
      },
      "PasswordChangeRequest": {
        "type": "object",
        "required": ["currentPassword", "newPassword"],
        "properties": {
          "currentPassword": { "type": "string", "format": "password" },
          "newPassword": { "type": "string", "format": "password", "minLength": 8 }
        }
      },
      "Address": {
        "type": "object",
        "properties": {
          "id": { "type": "integer", "format": "int64" },
          "street": { "type": "string" },
          "city": { "type": "string" },
          "state": { "type": "string" },
          "country": { "type": "string" },
          "postalCode": { "type": "string" },
          "isDefault": { "type": "boolean" }
        }
      },
      "AddressRequest": {
        "type": "object",
        "required": ["street", "city", "state", "country", "postalCode"],
        "properties": {
          "street": { "type": "string" },
          "city": { "type": "string" },
          "state": { "type": "string" },
          "country": { "type": "string" },
          "postalCode": { "type": "string" },
          "isDefault": { "type": "boolean", "default": false }
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
      },
      "MerchantRegistrationRequest": {
        "type": "object",
        "required": ["name", "email", "password"],
        "properties": {
          "name": { "type": "string", "description": "Business name" },
          "email": { "type": "string", "format": "email" },
          "password": { "type": "string", "format": "password" },
          "phone": { "type": "string" }
        }
      },
      "MerchantProfileResponse": {
        "type": "object",
        "properties": {
          "id": { "type": "integer", "format": "int64" },
          "businessName": { "type": "string" },
          "email": { "type": "string" },
          "phone": { "type": "string" },
          "active": { "type": "boolean" },
          "createdAt": { "type": "string", "format": "date-time" }
        }
      },
      "LoginRequest": {
        "type": "object",
        "required": ["email", "password"],
        "properties": {
          "email": { "type": "string" },
          "password": { "type": "string" }
        }
      },
      "AuthResponse": {
        "type": "object",
        "properties": {
          "accessToken": { "type": "string" },
          "tokenType": { "type": "string" },
          "expiresAt": { "type": "string", "format": "date-time" },
          "merchant": { "$ref": "#/components/schemas/MerchantProfileResponse" }
        }
      },
      "MerchantStoreRequest": {
        "type": "object",
        "required": ["storeName"],
        "properties": {
          "storeName": { "type": "string" },
          "storeCode": { "type": "string" },
          "logoUrl": { "type": "string" },
          "description": { "type": "string" },
          "storePhone": { "type": "string" },
          "currency": { "type": "string" },
          "defaultLanguage": { "type": "string" },
          "isActive": { "type": "boolean" },
          "email": { "type": "string", "format": "email" },
          "address": { "type": "string" }
        }
      },
      "MerchantStoreResponse": {
        "type": "object",
        "properties": {
          "id": { "type": "integer", "format": "int64" },
          "merchantId": { "type": "integer", "format": "int64" },
          "storeName": { "type": "string" },
          "description": { "type": "string" },
          "storeEmail": { "type": "string" },
          "storePhone": { "type": "string" },
          "currency": { "type": "string" },
          "language": { "type": "string" },
          "active": { "type": "boolean" },
          "street": { "type": "string" },
          "city": { "type": "string" },
          "state": { "type": "string" },
          "country": { "type": "string" },
          "postalCode": { "type": "string" },
          "createdAt": { "type": "string", "format": "date-time" },
          "updatedAt": { "type": "string", "format": "date-time" }
        }
      },
      "MerchantProductCreateRequest": {
        "type": "object",
        "required": ["name", "sku", "price", "stockQuantity"],
        "properties": {
          "name": { "type": "string", "example": "Gaming Laptop" },
          "sku": { "type": "string", "example": "LAP-001" },
          "description": { "type": "string", "example": "High-performance gaming laptop" },
          "price": { "type": "number", "format": "double", "example": 1299.99 },
          "stockQuantity": { "type": "integer", "example": 50 },
          "lowStockThreshold": { "type": "integer", "example": 10 },
          "categoryId": { "type": "integer", "format": "int64", "example": 1 }
        }
      },
      "InventoryItemResponse": {
        "type": "object",
        "properties": {
          "id": { "type": "integer", "format": "int64", "example": 1 },
          "storeId": { "type": "integer", "format": "int64", "example": 1 },
          "storeName": { "type": "string", "example": "Tech Store" },
          "name": { "type": "string", "example": "Gaming Laptop" },
          "sku": { "type": "string", "example": "LAP-001" },
          "description": { "type": "string", "example": "High-performance gaming laptop" },
          "price": { "type": "number", "format": "double", "example": 1299.99 },
          "stockQuantity": { "type": "integer", "example": 50 },
          "lowStockThreshold": { "type": "integer", "example": 10 },
          "isLowStock": { "type": "boolean", "example": false },
          "categoryId": { "type": "integer", "format": "int64", "example": 1 },
          "createdAt": { "type": "string", "format": "date-time" },
          "updatedAt": { "type": "string", "format": "date-time" }
        }
      },
      "InventoryUpdateRequest": {
        "type": "object",
        "properties": {
          "name": { "type": "string", "example": "Gaming Laptop Pro" },
          "description": { "type": "string", "example": "Updated description" },
          "price": { "type": "number", "format": "double", "example": 1399.99 },
          "stockQuantity": { "type": "integer", "example": 45 },
          "lowStockThreshold": { "type": "integer", "example": 15 },
          "categoryId": { "type": "integer", "format": "int64", "example": 1 }
        }
      },
      "SalesReportResponse": {
        "type": "object",
        "properties": {
          "storeId": { "type": "integer", "format": "int64" },
          "storeName": { "type": "string" },
          "startDate": { "type": "string", "format": "date" },
          "endDate": { "type": "string", "format": "date" },
          "totalOrders": { "type": "integer" },
          "completedOrders": { "type": "integer" },
          "cancelledOrders": { "type": "integer" },
          "totalRevenue": { "type": "number", "format": "double" },
          "averageOrderValue": { "type": "number", "format": "double" },
          "topProducts": {
            "type": "array",
            "items": { "$ref": "#/components/schemas/ProductReportResponse" }
          }
        }
      },
      "ProductReportResponse": {
        "type": "object",
        "properties": {
          "productId": { "type": "integer", "format": "int64" },
          "productName": { "type": "string" },
          "sku": { "type": "string" },
          "unitsSold": { "type": "integer" },
          "totalRevenue": { "type": "number", "format": "double" },
          "orderCount": { "type": "integer" }
        }
      },
      "ProductAnalyticsResponse": {
        "type": "object",
        "properties": {
          "productId": { "type": "integer", "format": "int64" },
          "productName": { "type": "string" },
          "sku": { "type": "string" },
          "startDate": { "type": "string", "format": "date" },
          "endDate": { "type": "string", "format": "date" },
          "totalUnitsSold": { "type": "integer" },
          "totalRevenue": { "type": "number", "format": "double" },
          "averageUnitPrice": { "type": "number", "format": "double" },
          "totalOrders": { "type": "integer" },
          "pageViews": { "type": "integer" },
          "conversionRate": { "type": "number", "format": "double" },
          "dailyMetrics": {
            "type": "array",
            "items": { "$ref": "#/components/schemas/DailySalesMetric" }
          }
        }
      },
      "DailySalesMetric": {
        "type": "object",
        "properties": {
          "date": { "type": "string", "format": "date" },
          "unitsSold": { "type": "integer" },
          "revenue": { "type": "number", "format": "double" },
          "orders": { "type": "integer" },
          "views": { "type": "integer" }
        }
      }
    }
  }
}
                """;
    }
}
