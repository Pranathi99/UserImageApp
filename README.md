###### UserImageApp ######

This is a Spring Boot application that allows users to register with their first name, last name, username, and password. Once registered, users are authorized with Imgur’s OAuth,
and an access token is generated, which they can use to make authenticated API calls. The app integrates with Imgur’s API to manage images, providing users with the ability to upload,
view, and delete images.

### Features ###
1. User Registration: Users can register with their first name, last name, username, and password.
2. OAuth Authorization: Once registered, users can authorize the app with Imgur to generate an access token for API calls.
3. Image Management: 
     Upload images to Imgur after successful authentication.
     View images uploaded by the user from Imgur.
     Delete images from Imgur using the image’s unique hash.
4. User Profile: View user details along with their uploaded images.

### Prerequisites ###
JDK 17 or higher
SpringBoot 3.x.x
Maven
H2 database for storing user details (username, password)
Imgur account for image management
SLF4J for logging purposes
Spring Security for authentication
Imgur API client credentials (client ID, client secret, redirect URI)

### Workflow ###
1. User Registration:
Users provide their first name, last name, username, and password via a REST API.
The system checks if the username already exists. If not, the user is successfully registered.

2. Imgur OAuth Authorization:
After registration, the user is redirected to Imgur’s OAuth 2.0 authorization page.
Imgur redirects the user back with an authorization code.

3. Access Token Generation:
The app exchanges the authorization code for an OAuth access token from Imgur.
The access token is then stored for making authorized API calls.

4. Image Operations:
Using the access token, the user can:
  Upload Images to Imgur.
  View Images uploaded by the user.
  Delete Images by specifying the image hash.

5. Profile and Image List:
The user can view their basic profile information (first name, last name, username).
They can also retrieve a list of images that they have uploaded to Imgur.

### API Endpoints ###
1. User Registration
POST /api/register
Parameters: firstname, lastname, username, password
Response:
200 OK: User successfully registered.
400 Bad Request: Username already exists.
   
2. Imgur OAuth Authorization
GET /api/authorize
Parameters: username
Response:
Redirects to Imgur’s OAuth authorization page.

3. OAuth Callback and Access Token
GET /api/callbac
Parameters: Authorization Code (from Imgur)
Response:
200 OK: Access token returned successfully.
500 Internal Server Error: If there was an issue retrieving the token.
   
4. Upload Image
POST /api/upload-image
Parameters: file (The image file to upload), Access Token
Response:
200 OK: Image uploaded successfully. Returns the image URL.
403 Forbidden: User not authenticated.
500 Internal Server Error: If image upload fails.
   
5. View Image
GET /api/{imageId}
Parameters: imageId, Access Token
Response:
200 OK: Image details returned successfully.
403 Forbidden: User not authenticated.
404 Not Found: Image not found.
   
6. Delete Image
DELETE /api/{deleteHash}
Parameters: deleteHash (The hash of the image to delete), Access Token
Response:
200 OK: Image deleted successfully.
403 Forbidden: User not authenticated.
404 Not Found: Image not found.
   
7. View All Images
GET /api/user/images
Parameters: Access Token
Response:
200 OK: List of user images returned.
403 Forbidden: User not authenticated.
500 Internal Server Error: If image retrieval fails.
   
8. View User Details
GET /api/user-details
Parameters: username, Access Token
Response:
200 OK: User details returned successfully.
403 Forbidden: User not authenticated.

### Setup ###
1. Clone the Repository:
git clone https://github.com/Pranathi99/UserImageApp.git

2. Install Dependencies:
mvn clean install

3. Configure Imgur API Credentials: (in application.properties file)
imgur.clientId=your-imgur-client-id
imgur.clientSecret=your-imgur-client-secret
imgur.redirectUri=your-imgur-redirect-uri

4. Run the Application:
mvn spring-boot:run

5. Access the API: http://localhost:8080/api/



### OUTPUT SNIPPETS ###

1. Register User
<img width="888" alt="Screenshot 2025-01-23 at 1 28 00 AM" src="https://github.com/user-attachments/assets/81f46a75-055b-42de-8f08-72a06daf98e5" />

2.Authorization with Imgur
<img width="884" alt="Screenshot 2025-01-23 at 1 29 38 AM" src="https://github.com/user-attachments/assets/691475cd-871f-4eba-a8a0-edd2af99cfc3" />
<img width="1463" alt="Screenshot 2025-01-23 at 1 30 00 AM" src="https://github.com/user-attachments/assets/ecc945bb-bdc5-460a-8631-323731f8bdca" />

3. Access Token Generation
<img width="1461" alt="Screenshot 2025-01-23 at 1 30 14 AM" src="https://github.com/user-attachments/assets/0362efd3-4eaf-4cf4-842e-8aab091511b4" />
 
4. Image Upload to Imgur
<img width="900" alt="Screenshot 2025-01-23 at 1 33 08 AM" src="https://github.com/user-attachments/assets/5c99ab4b-4858-400a-8d4d-540a7a155455" />
<img width="894" alt="Screenshot 2025-01-23 at 1 33 32 AM" src="https://github.com/user-attachments/assets/aad97629-3326-4968-83f8-43d69fb63f65" />
<img width="1169" alt="Screenshot 2025-01-23 at 1 34 47 AM" src="https://github.com/user-attachments/assets/9560a015-8383-46c7-a17c-5411d47027ab" />

5. View Image from Imgur using imageId
<img width="890" alt="Screenshot 2025-01-23 at 1 39 42 AM" src="https://github.com/user-attachments/assets/58aebd73-7f36-4580-abf4-7b57c47bd5b1" />

6. Delete Image from Imgur
<img width="894" alt="Screenshot 2025-01-23 at 1 42 19 AM" src="https://github.com/user-attachments/assets/6cfcc86b-7589-4831-b5b2-6f7fcdc9448f" />

7. View all images of a user
<img width="879" alt="Screenshot 2025-01-23 at 1 44 54 AM" src="https://github.com/user-attachments/assets/e3e3b447-b05a-452f-8fb7-b10c9307a180" />

8. View User details
<img width="894" alt="Screenshot 2025-01-23 at 1 53 48 AM" src="https://github.com/user-attachments/assets/10414e01-6c4b-47d8-a6a5-1a34c06feedf" />

9. When user tries to authorizes without registering
<img width="895" alt="Screenshot 2025-01-23 at 1 45 55 AM" src="https://github.com/user-attachments/assets/d30ed3b6-efd3-44af-b7f3-fe174f4ea746" />

10. Logging
<img width="1142" alt="Screenshot 2025-01-23 at 2 45 32 AM" src="https://github.com/user-attachments/assets/216ce70a-6e0d-49c5-a078-95489738be45" />

11. Image in Imgur
<img width="1469" alt="Screenshot 2025-01-23 at 9 06 10 AM" src="https://github.com/user-attachments/assets/d1a3ed7d-87c0-4073-9034-66037e3e7d5c" />
