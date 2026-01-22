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
        "summary": "View cart contents",
        "description": "FR-007: View cart contents. Returns the customer's shopping cart with all items, quantities, and calculated totals.",
        "parameters": [
          {
            "name": "customerId",
            "in": "query",
            "required": true,
            "description": "Customer ID",
            "schema": { "type": "integer", "format": "int64" }
          }
        ],
        "responses": {
          "200": {
            "description": "Cart retrieved successfully",
            "content": {
              "application/json": {
                "schema": { "$ref": "#/components/schemas/CartResponse" }
              }
            }
          },
          "400": { "description": "customerId is required" }
        }
      },
      "delete": {
        "tags": ["Cart"],
        "summary": "Clear cart",
        "description": "FR-008: Clear entire cart. Removes all items from the customer's shopping cart.",
        "parameters": [
          {
            "name": "customerId",
            "in": "query",
            "required": true,
            "description": "Customer ID",
            "schema": { "type": "integer", "format": "int64" }
          }
        ],
        "responses": {
          "204": { "description": "Cart cleared successfully" },
          "400": { "description": "customerId is required" }
        }
      }
    },
    "/cart/items": {
      "post": {
        "tags": ["Cart"],
        "summary": "Add item to cart",
        "description": "FR-006: Add item to cart. Validates product exists and has sufficient stock before adding.",
        "requestBody": {
          "required": true,
          "content": {
            "application/json": {
              "schema": { "$ref": "#/components/schemas/AddToCartRequest" }
            }
          }
        },
        "responses": {
          "201": {
            "description": "Item added to cart",
            "content": {
              "application/json": {
                "schema": { "$ref": "#/components/schemas/CartResponse" }
              }
            }
          },
          "400": { "description": "Invalid request or insufficient stock" }
        }
      }
    },
    "/cart/items/{itemId}": {
      "put": {
        "tags": ["Cart"],
        "summary": "Update cart item quantity",
        "description": "FR-008: Update cart item. Changes the quantity of an item in the cart.",
        "parameters": [
          {
            "name": "itemId",
            "in": "path",
            "required": true,
            "description": "Cart item ID",
            "schema": { "type": "integer", "format": "int64" }
          },
          {
            "name": "customerId",
            "in": "query",
            "required": true,
            "description": "Customer ID",
            "schema": { "type": "integer", "format": "int64" }
          },
          {
            "name": "quantity",
            "in": "query",
            "required": true,
            "description": "New quantity",
            "schema": { "type": "integer", "minimum": 1 }
          }
        ],
        "responses": {
          "200": {
            "description": "Cart item updated",
            "content": {
              "application/json": {
                "schema": { "$ref": "#/components/schemas/CartResponse" }
              }
            }
          },
          "400": { "description": "Invalid request or insufficient stock" },
          "404": { "description": "Cart item not found" }
        }
      },
      "delete": {
        "tags": ["Cart"],
        "summary": "Remove item from cart",
        "description": "FR-008: Remove item from cart. Deletes a specific item from the customer's cart.",
        "parameters": [
          {
            "name": "itemId",
            "in": "path",
            "required": true,
            "description": "Cart item ID",
            "schema": { "type": "integer", "format": "int64" }
          },
          {
            "name": "customerId",
            "in": "query",
            "required": true,
            "description": "Customer ID",
            "schema": { "type": "integer", "format": "int64" }
          }
        ],
        "responses": {
          "204": { "description": "Item removed from cart" },
          "400": { "description": "customerId is required" },
          "404": { "description": "Cart item not found" }
        }
      }
    },
    "/cart/total": {
      "get": {
        "tags": ["Cart"],
        "summary": "Calculate cart total",
        "description": "FR-009: Calculate cart total. Returns the total price of all items in the cart.",
        "parameters": [
          {
            "name": "customerId",
            "in": "query",
            "required": true,
            "description": "Customer ID",
            "schema": { "type": "integer", "format": "int64" }
          }
        ],
        "responses": {
          "200": {
            "description": "Total calculated",
            "content": {
              "application/json": {
                "schema": {
                  "type": "object",
                  "properties": {
                    "total": { "type": "number", "format": "double" }
                  }
                }
              }
            }
          },
          "400": { "description": "customerId is required" }
        }
      }
    },
    "/cart/count": {
      "get": {
        "tags": ["Cart"],
        "summary": "Get cart item count",
        "description": "Returns the total number of items in the cart (sum of quantities).",
        "parameters": [
          {
            "name": "customerId",
            "in": "query",
            "required": true,
            "description": "Customer ID",
            "schema": { "type": "integer", "format": "int64" }
          }
        ],
        "responses": {
          "200": {
            "description": "Item count retrieved",
            "content": {
              "application/json": {
                "schema": {
                  "type": "object",
                  "properties": {
                    "count": { "type": "integer" }
                  }
                }
              }
            }
          },
          "400": { "description": "customerId is required" }
        }
      }
    },
    "/cart/validate": {
      "post": {
        "tags": ["Cart"],
        "summary": "Validate cart for checkout",
        "description": "FR-009: Validate cart before checkout. Checks product availability, stock levels, and price changes.",
        "parameters": [
          {
            "name": "customerId",
            "in": "query",
            "required": true,
            "description": "Customer ID",
            "schema": { "type": "integer", "format": "int64" }
          }
        ],
        "responses": {
          "200": {
            "description": "Validation result",
            "content": {
              "application/json": {
                "schema": { "$ref": "#/components/schemas/CartValidationResponse" }
              }
            }
          },
          "400": { "description": "customerId is required" }
        }
      }
    },
    "/cart/merge": {
      "post": {
        "tags": ["Cart"],
        "summary": "Merge anonymous cart with customer cart",
        "description": "Merges an anonymous cart into a logged-in customer's cart. Used when a guest user logs in.",
        "parameters": [
          {
            "name": "anonymousCartId",
            "in": "query",
            "required": true,
            "description": "Anonymous cart ID to merge from",
            "schema": { "type": "integer", "format": "int64" }
          },
          {
            "name": "customerId",
            "in": "query",
            "required": true,
            "description": "Customer ID to merge into",
            "schema": { "type": "integer", "format": "int64" }
          }
        ],
        "responses": {
          "200": {
            "description": "Carts merged successfully",
            "content": {
              "application/json": {
                "schema": { "$ref": "#/components/schemas/CartResponse" }
              }
            }
          },
          "400": { "description": "Both anonymousCartId and customerId are required" }
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
      "CartResponse": {
        "type": "object",
        "description": "Shopping cart with items and totals",
        "properties": {
          "id": { "type": "integer", "format": "int64", "description": "Cart ID" },
          "customerId": { "type": "integer", "format": "int64", "description": "Customer ID" },
          "items": {
            "type": "array",
            "description": "List of items in the cart",
            "items": { "$ref": "#/components/schemas/CartItemResponse" }
          },
          "totalItems": { "type": "integer", "description": "Total number of items (sum of quantities)" },
          "subtotal": { "type": "number", "format": "double", "description": "Subtotal before any discounts" },
          "total": { "type": "number", "format": "double", "description": "Total amount" },
          "createdAt": { "type": "string", "format": "date-time" },
          "updatedAt": { "type": "string", "format": "date-time" }
        }
      },
      "CartItemResponse": {
        "type": "object",
        "description": "Individual cart item with product details",
        "properties": {
          "id": { "type": "integer", "format": "int64", "description": "Cart item ID" },
          "productId": { "type": "integer", "format": "int64", "description": "Product ID" },
          "productName": { "type": "string", "description": "Product name" },
          "productImageUrl": { "type": "string", "description": "Product image URL" },
          "quantity": { "type": "integer", "description": "Quantity in cart" },
          "price": { "type": "number", "format": "double", "description": "Unit price" },
          "subtotal": { "type": "number", "format": "double", "description": "Line item subtotal (price * quantity)" },
          "inStock": { "type": "boolean", "description": "Whether product is in stock" },
          "availableStock": { "type": "integer", "description": "Available stock quantity" }
        }
      },
      "AddToCartRequest": {
        "type": "object",
        "description": "Request to add item to cart",
        "required": ["customerId", "productId", "quantity"],
        "properties": {
          "customerId": { "type": "integer", "format": "int64", "description": "Customer ID" },
          "productId": { "type": "integer", "format": "int64", "description": "Product ID to add" },
          "quantity": { "type": "integer", "minimum": 1, "description": "Quantity to add" },
          "price": { "type": "number", "format": "double", "description": "Optional price override" }
        }
      },
      "CartValidationResponse": {
        "type": "object",
        "description": "Cart validation result for checkout",
        "properties": {
          "valid": { "type": "boolean", "description": "Whether cart is valid for checkout" },
          "errors": {
            "type": "array",
            "description": "List of validation errors (cart cannot proceed to checkout)",
            "items": { "type": "string" }
          },
          "warnings": {
            "type": "array",
            "description": "List of warnings (cart can proceed but user should be aware)",
            "items": { "type": "string" }
          }
        }
      },
      "Cart": {
        "type": "object",
        "deprecated": true,
        "description": "Deprecated: Use CartResponse instead",
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
        "deprecated": true,
        "description": "Deprecated: Use CartItemResponse instead",
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
      }
    }
  }
}
                """;
    }
}
