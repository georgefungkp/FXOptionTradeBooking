# **FX Option Trade Booking System**

A comprehensive Spring Boot application designed for managing Foreign Exchange (FX) option trade bookings in investment banking and financial services. This system provides secure, scalable, and robust trade management capabilities with enterprise-grade features.

## Developer Information

**Name:** George Fung

**Email:** georgefungkp@gmail.com

**GitHub Username:** georgefungkp

**LinkedIn Profile:** https://www.linkedin.com/in/george-fung

## **Features**

### **Core Trading Operations**
- **Trade Booking**: Complete FX option trade booking with comprehensive validation
- **Trade Management**: Full CRUD operations for trade lifecycle management
- **Counterparty Management**: Complete counterparty onboarding and management
- **Status Tracking**: Real-time trade status monitoring (PENDING, CONFIRMED, SETTLED, CANCELLED, EXPIRED)
- **Option Types**: Support for CALL and PUT options with proper pricing calculations

### **Advanced Features**
- **Multi-level Validation**: Business rules validation, counterparty verification, and data integrity checks
- **Audit Trail**: Complete audit logging with creation and modification timestamps
- **Pagination Support**: Efficient handling of large datasets with Spring Data pagination
- **Search & Filtering**: Advanced search capabilities by counterparty, status, date ranges
- **Premium Calculations**: Automatic FX option premium calculations

### **Data Validation & Business Rules**
- **Currency Validation**: ISO currency code validation with support for major global currencies
- **Amount Validation**: Minimum/maximum notional amounts (10K to 1B) with precision control
- **Date Validation**: Business day validation, date sequence enforcement, maximum tenor limits
- **Strike Price Validation**: Reasonable strike price ranges and spot rate consistency checks
- **Premium Validation**: Premium amount and currency validation with market convention checks

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
- **Repositories**: Spring Data JPA repositories with custom queries
- **DTOs**: Request/Response objects for clean API contracts
- **Entities**: JPA entities with proper relationships and constraints
- **Security**: JWT-based authentication with method-level authorization
- **Validation**: Multi-layered validation with custom business rules


## **Business Domain Models**

### **Core Entities**

- **Trade**: Complete FX option trade with pricing, dates, and lifecycle management
- **Counterparty**: Trading partners with risk ratings and compliance status  
- **User**: System users with role-based permissions
- **OptionType**: CALL/PUT option specifications
- **TradeStatus**: Complete trade lifecycle states

### **Key Business Rules**

- **Trade Validation**: Strike price, notional amount, and date validations
- **Counterparty Verification**: Active status and credit limit checks
- **Status Transitions**: Controlled trade lifecycle management
- **Risk Controls**: Position limits and exposure monitoring

## **Security Implementation**

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


