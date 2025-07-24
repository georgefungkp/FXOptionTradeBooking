# **FX Derivatives Trade Booking System**

A comprehensive Spring Boot application designed for managing Foreign Exchange (FX) and derivative trade bookings in investment banking and financial services. This system provides secure, scalable, and robust trade management capabilities with enterprise-grade features and comprehensive observability.

## Developer Information

**Name:** George Fung

**Email:** georgefungkp@gmail.com

**GitHub Username:** georgefungkp

**LinkedIn Profile:** https://www.linkedin.com/in/george-fung

## **Features**

### **Core Trading Operations**
- **Multi-Product Trade Booking**: Complete trade booking system supporting multiple financial instruments
- **Trade Management**: Full CRUD operations for trade lifecycle management
- **Counterparty Management**: Complete counterparty onboarding and management
- **Status Tracking**: Real-time trade status monitoring (PENDING, CONFIRMED, SETTLED, CANCELLED, EXPIRED)
- **Product-Specific Validation**: Tailored validation rules for each product type

### **Supported Financial Products** 💰

#### **FX Options**
- **Vanilla Options**: Standard CALL and PUT options with comprehensive pricing calculations
- **Exotic Options**: Advanced option structures including:
  - **Barrier Options**: Knock-in/knock-out features with barrier level monitoring
  - **Asian Options**: Average rate options with observation frequency settings
  - **Digital Options**: Binary payout structures with trigger conditions

#### **FX Contracts**
- **FX Forward**: Foreign exchange forward contracts with maturity-based pricing
- **FX Spot**: Immediate settlement foreign exchange transactions

#### **Swap Products**
- **FX Swaps**: Currency pair swaps with near-leg and far-leg settlements
- **Currency Swaps**: Cross-currency interest rate exchanges
- **Interest Rate Swaps**: Fixed-to-floating rate exchanges with multiple index support (SOFR, LIBOR, EURIBOR, SONIA, TONAR)

### **Advanced Features**
- **Multi-level Validation**: Business rules validation, counterparty verification, and data integrity checks
- **Product-Specific Validators**: Specialized validation logic for each financial instrument
- **Audit Trail**: Complete audit logging with creation and modification timestamps
- **Pagination Support**: Efficient handling of large datasets with Spring Data pagination
- **Search & Filtering**: Advanced search capabilities by counterparty, status, date ranges, product types
- **Comprehensive Pricing**: Automatic calculations for premiums, rates, and settlements

### **Observability & Monitoring** 🔍
- **Distributed Tracing**: OpenTelemetry integration with Jaeger for request flow visualization
- **Custom Span Creation**: `@Observed` annotations for business logic tracing
- **Metrics Collection**: Prometheus metrics for performance monitoring
- **Application Monitoring**: Spring Boot Actuator endpoints for health checks
- **Trace Context Propagation**: Complete request tracing across all components
- **Custom Attributes**: Rich span metadata for business operations

### **Data Validation & Business Rules**
- **Currency Validation**: ISO currency code validation with support for major global currencies
- **Amount Validation**: Minimum/maximum notional amounts (10K to 1B) with precision control
- **Date Validation**: Business day validation, date sequence enforcement, maximum tenor limits
- **Rate Validation**: Strike prices, forward rates, swap rates with market convention checks
- **Product-Specific Rules**: Tailored validation for barrier levels, settlement dates, index rates
- **Cross-Field Validation**: Consistency checks across related fields

### **Security & Compliance**
- **JWT Authentication**: Stateless token-based security architecture
- **Role-based Authorization**: Granular access control for different user types
- **Data Encryption**: Secure password hashing and sensitive data protection
- **Regulatory Compliance**: Audit trails and data retention policies

## **Technologies Used**

### **Core Framework**
- **Spring Boot 3.5.3** - Latest enterprise application framework
- **Java 22** - Modern Java features and performance optimizations
- **Jakarta EE** - Enterprise Java specifications

### **Spring Ecosystem**
- **Spring Security 6.5.1** - Advanced security framework
- **Spring Data JPA 3.5.1** - Data persistence and repository abstraction
- **Spring Data REST** - Restful web services automation
- **Spring AOP** - Aspect-oriented programming for cross-cutting concerns

### **Observability Stack** 📊
- **OpenTelemetry 1.49.0** - Cloud-native observability framework
- **Micrometer Tracing** - Application metrics and tracing abstraction
- **Micrometer Observation** - `@Observed` annotation support for custom spans
- **Prometheus Metrics** - Time-series metrics collection and export
- **Spring Boot Actuator** - Production-ready monitoring and management
- **Jaeger Tracing** - Distributed tracing UI and analysis

### **Database & Persistence**
- **Hibernate 6.6.18** - Advanced ORM with Jakarta persistence
- **H2 Database** - In-memory database for development and testing

### **Security & Authentication**
- **JWT (JSON Web Tokens)** - Stateless authentication mechanism
- **BCrypt** - Industry-standard password hashing
- **Spring Security Test** - Comprehensive security testing support

### **Testing Framework**
- **JUnit 5** - Modern testing framework with advanced features
- **Mockito 5.17.0** - Comprehensive mocking and test doubles
- **Spring Boot Test** - Integration testing with auto-configuration
- **Spring Security Test** - Security-specific testing utilities

### **Utilities & Tools**
- **Lombok 1.18.38** - Boilerplate code reduction
- **ModelMapper 3.1.1** - Advanced object mapping and transformation
- **Jackson** - High-performance JSON processing
- **Maven** - Project management and dependency resolution

## **Architecture Overview**

### **Key Architectural Components**
- **Controllers**: Restful API endpoints with proper HTTP semantics
- **Services**: Business logic implementation with transaction management
- **Product Validators**: Strategy pattern implementation for product-specific validation
- **Repositories**: Spring Data JPA repositories with custom queries
- **DTOs**: Request/Response objects for clean API contracts
- **Entities**: JPA entities with proper relationships and constraints
- **Security**: JWT-based authentication with method-level authorization
- **Validation**: Multi-layered validation with custom business rules
- **Observability**: Comprehensive monitoring and tracing infrastructure

### **Product Validation Strategy**
The system implements a flexible validation architecture using the Strategy pattern:
- **ProductValidator Interface**: Common validation contract
- **Product-Specific Validators**:
  - `VanillaOptionValidator`: Standard option validation
  - `ExoticOptionValidator`: Complex option structure validation
  - `FXContractValidator`: FX forward and spot validation
  - `SwapValidator`: Multi-type swap validation with sub-validators

### **Authentication Flow**
1. User credentials validation
2. JWT token generation with claims
3. Token-based request authentication
4. Role-based authorization checks

### **Security Features**
- **Stateless Architecture**: No server-side session management
- **Token Expiration**: Configurable token lifecycle
- **Role Hierarchy**: ADMIN, TRADER, VIEWER permissions
- **CORS Configuration**: Cross-origin request handling
- **CSRF Protection**: Cross-site request forgery prevention

## **API Endpoints**

### **Trade Management**
- `POST /api/trades` - Book new trades (all product types)
- `GET /api/trades/{id}` - Retrieve trade by ID
- `GET /api/trades/reference/{reference}` - Find trade by reference
- `PUT /api/trades/{id}/status` - Update trade status
- `DELETE /api/trades/{id}` - Cancel trade

### **Product-Specific Queries**
- `GET /api/trades/vanilla-options` - List vanilla options
- `GET /api/trades/exotic-options` - List exotic options
- `GET /api/trades/fx-contracts` - List FX forwards and spots
- `GET /api/trades/swaps` - List all swap products
- `GET /api/trades/swaps/type/{swapType}` - Filter swaps by type

### **Advanced Filtering**
- `GET /api/trades/counterparty/{id}` - Trades by counterparty
- `GET /api/trades/status/{status}` - Trades by status
- `GET /api/trades/product-type/{type}` - Trades by product type
- `GET /api/trades/date-range` - Trades within date range

## **Getting Started**

### **Prerequisites**
- Java 22 or higher
- Maven 3.6 or higher
- IDE with Spring Boot support (IntelliJ IDEA recommended)

### **Installation**
1. Clone the repository
2. Navigate to project directory
3. Run `mvn clean install`
4. Start the application: `mvn spring-boot:run`

### **Default Configuration**
- **Server Port**: 8080
- **Database**: H2 in-memory (auto-configured)
- **Security**: JWT tokens (1 hour expiration)
- **Observability**: Enabled with Jaeger export

### **Sample Data**
The application includes comprehensive sample data initialization:
- Multiple counterparties across different regions
- Sample trades for all product types
- User accounts with different roles
- Realistic market data and rates

