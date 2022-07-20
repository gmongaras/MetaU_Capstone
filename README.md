# Overview
- [Project Description](#project-description)
- [Project Requirements](#project-requirements)
- [Cloning The Project](#cloning-the-project)
- [Executing The Project](#executing-the-project)
- [View Descriptions](#view-descriptions)
  - [Login Page](#login-page)
  - [Register Page](#register-page)
  - [Main Menu](#main-menu)
  - [Homepage - Countdown](#homepage---countdown)
  - [Homepage - Fortune](#homepage---fortune)
  - [Map](#map)
  - [Profile - Logged in user](#profile---logged-in-user)
  - [Profile Menu - Logged in user](#profile-menu---logged-in-user)
  - [Friends](#friends)
  - [Profile - Friend/Other User](#profile-menu---friendother-user)
  - [Profile Menu - Friend/Other User](#profile-menu---friendother-user)
  - [Settings](#settings)
    - Supported Languages
  - [Fortune Detailed View](#fortune-detailed-view)
- [Offline Mode](#offline-mode)


# Project Description
This is my final project for Meta University! The goal is to code an Android app of our choice that fulfills several 
requirements such as using an API and using a database.

Additionally, I am working on this project to learn more about Android!

This app gives a fortune a day from a virtual fortune cookie. The fortune can then be shared with other users and a 
the user can see a map of where they received each fortune.

There are two modes in which the user can chose to receive a fortune, either through an AI or through a list of real fortunes. 
The AI comes from antoher project I am working on which should be able to generate fortunes given a matrix of random noise. The 
AI project [can be found here](https://github.com/gmongaras/PyTorch_TextGen).

I used this project to learn more about integrating PyTorch models into the app, which [I wrote an article about](https://gmongaras.medium.com/integrating-custom-pytorch-models-into-an-android-app-a2cdfce14fe8).


# Project Requirements
The project requires Android 21 or above to run and uses the following packages:
- org.pytorchpytorch_android_lite: 1.12
- com.squareup.okhttp3:logging-interceptor: 4.7.2
- com.github.bumptech.glide:glide: 4.12.0
- com.github.bumptech.glide:compiler: 4.12.0
- com.google.android.gms:play-services-maps: 18.0.2
- com.google.android.gms:play-services-location: 20.0.0
- androidx.appcompat:appcompat: 1.4.2
- com.github.parse-community.Parse-SDK-Android:parse: 3.0.1
- com.github.parse-community.Parse-SDK-Android:fcm: 3.0.1
- com.airbnb.android:lottie: 3.0.1
- androidx.swiperefreshlayout:swiperefreshlayout: 1.1.0
- androidx.room:room-runtime:2.4.2
- com.google.mlkit:translate:17.0.0
- com.google.android.gms:play-services-mlkit-language-id:17.0.0-beta1

Additionally, the following permissions are used:
- ACCESS_NETWORK_STATE
- INTERNET
- ACCESS_FINE_LOCATION
- ACCESS_COARSE_LOCATION
- ACCESS_BACKGROUND_LOCATION
- SCHEDULE_EXACT_ALARM

The following APIs are used:
- [Parse](https://parseplatform.org/)
- [Google Maps](https://developers.google.com/maps)
- [Google Cloud Translation](https://cloud.google.com/translate/docs)



# Cloning The Project
The following section will describe how to clone the project and set it up to begin making it your own!

Note: I am using Android Studio version: 2021.2.1 Patch 1 (chipmunk(

1. [Install android studio](https://developer.android.com/studio/?gclsrc=aw.ds&gclid=Cj0KCQjwzqSWBhDPARIsAK38LY-dqK0iXiXQnG7ZU9Ccw3crlRkECsOyKavCRl_0pQ7Z9HKs6qyunngaAtWhEALw_wcB) on your device
2. Clone this project from Github with the following command `git clone https://github.com/gmongaras/MetaU_Capstone.git`
3. Open the cloned project in android studio with `MetaU_Capstone` as the root directory for the project.
4. In `MetaU_Capstone/app/src/main/res/values/`, add a new file named `secrets.xml`.
5. Within `secrets.xml`, paste the following code in the file: 
```
<?xml version="1.0" encoding="utf-8"?>
<resources>

    <string name="google_maps_api_key">Your maps API key</string>

    <string name="back4app_server_url">https://parseapi.back4app.com</string>
    <string name="back4app_client_key">Your Parse API key</string>
    <string name="back4app_app_id">Your Parse app id</string>

</resources>
```
6. Go to [the following link](https://developers.google.com/maps), create a new project, and replace "Your maps API key" in `secrets.xml` with your google maps API key.
7. Go to [the following link](https://www.back4app.com/) (or any other parse database), create a new project, replace "Your Parse API key" in `secrets.xml` with your Parse API key, and replace "Your Parse app id" in `secrets.xml` with your Parse app id.
8. Build the app, and the app should successfully build.
9. Setup the Parse database with the following classes and attributes:

<b>User</b>
- objectId [String]
- ACL [ACL]
- updatedAt [Date]
- createdAt [Date]
- authData [Object][Required]
- username [String][Required]
- password [String][Required]
- fortunes [Relation<Fortune>]
- friends [Relation<User>]
- profilePic [File]
- friend_requests [Relation<User>]
- sent_requests [Relation<User>]
- showFortunesFriends [Boolean]
- showFortunesUsers [Boolean]
- showMapFriends [Boolean]
- showMapUsers [Boolean]
- pushNotifications [Boolean]
- friendable [Boolean]
- useAI [Boolean]
- darkMode [Boolean]
- Blocked [Relation<User>]
- liked [Relation<Fortune>]
- lang [String]

<b>Fortune</b>
- objectId [String]
- updatedAt [Date]
- createdAt [Date]
- ACL [ACL]
- user [Pointer<User>][Required]
- message [String][Required]
- location [GeoPoint]
- like_ct [Number]

<b>Friend_queue</b>
- objectId [String]
- updatedAt [Date]
- createdAt [Date]
- ACL [ACL]
- user [Pointer<User>]
- friend [Pointer<User>]
- mode [String]



# Executing The Project
   
To execute the project with an APK, go to the [APKs directory]() and download one of the APKs. Then, using the Android Installer on an Android phone, install the app.
   
To create an APK using Android Studio, in the Menu, click on `Build > Build Bundles(s) / APK(s) > Build APK(s)`. The APK file should be located in `MetaU_Capstone > app > build > outputs > apk` and should be called `app-debug.apk`.



# View Descriptions
There are a lot of views in this project, and I will go over each one below.

## View Table of Contents
- [Login Page](#login-page)
- [Register Page](#register-page)
- [Main Menu](#main-menu)
- [Homepage - Countdown](#homepage---countdown)
- [Homepage - Fortune](#homepage---fortune)
- [Map](#map)
- [Profile - Logged in user](#profile---logged-in-user)
- [Profile Menu - Logged in user](#profile-menu---logged-in-user)
- [Friends](#friends)
- [Profile - Friend/Other User](#profile-menu---friendother-user)
- [Profile Menu - Friend/Other User](#profile-menu---friendother-user)
- [Settings](#settings)
- [Fortune Detailed View](#fortune-detailed-view)

### Login Page
<p align="left"><img src="https://github.com/gmongaras/MetaU_Capstone/blob/main/Images/Login%20Page.png" height="400" title="Login Page"></p>
  
- <b>Description:</b> When the user firsts starts up the app, the user is greeted with a login page to log the user into the app. 
The user account is used to keep track of the user's fortunes, friends, etc.
  
- <b>Features:</b>
  1. The user is able to enter a username and password.
  2. A Login button allows the user to login to their account if the username exists and the password is correct. Upon login, the user is taken to their [homepage](#homepage---fortune)
  3. If the user doesn't have an account, the user can [register](#register-page) on a different page
  4. Pressing the back button exits the app

- <b>Note:</b> If the langauge is changed in the [Settings](#settings), then the app may take a little longer to load as it requires a model to be downloaded, but once the model is downloaded, it is kept on the phone until the langauge is changed again.

### Register Page
<p align="left"><img src="https://github.com/gmongaras/MetaU_Capstone/blob/main/Images/Register%20Page.png" height="400" title="Register Page"></p>
  
- <b>Description:</b> If the user wants to make a new account, they can do so by entering a new username and a new password.
- <b>Features:</b>
  
  1. The user is able to enter a username which will be used as that user's account username.
  2. The user can enter a password and reenter that password to create a new password for their new account.
  3. The user can select one of [many different languages](#supported-languages) for the app to be displayed in
  4. If the passwords match and the username is not already taken, a new user is created with the specified username as their new username and the specified password as their new password. The user is then logged into their account and sent to their [homepage](#homepage---fortune)
  5. Pressing the back button takes the user back to the login page

- <b>Note:</b> If the user selcts a language other than English to load the app in, then registration may take a little longer than usual as language models required for translation need to be downloaded. This is a one time download and only happens when the user selects a new langauge in [Settings](#settings) or when registering.

### Main Menu
<p align="left"><img src="https://github.com/gmongaras/MetaU_Capstone/blob/main/Images/Main%20Menu.png" height="400" title="Main Menu"></p>
  
- <b>Description:</b> The main menu allows the user to access 4 different views:
  1. [Map](#map) - The user's map showing where they received each fortune.
  2. [Home](#homepage---countdown) - The user's homepage to open a new fortune cookie or view the time left until a new fortune is availble.
  3. [Profile](#profile---logged-in-user) - The user's profile in which they can view their fortunes, search through their fortunes, change their settings, or logout.
  4. [Friends](#friends) - The user's friends page which shows the user's current friends, shows the user's friends requests, and allows the user to search for new friends.
  
- <b>Special Features:</b>
  - The user can swipe left or right to change to the view to the left or right of the current view.
  - Clicking on a menu item will take the user to that page.
  - Pressing back on the homepage exits the app while pressing back on any other page goes back to the main page.

### Homepage - Countdown
<p align="left"><img src="https://github.com/gmongaras/MetaU_Capstone/blob/main/Images/Homepage%20-%20Countdown.png" height="400" title="Homepage - Countdown"></p>
  
- <b>Description:</b> If the user is accessing the app within 23 hours after opening their last fortune, the user will see see a timer counting down until they can open their next fortune cookie.
  
- <b>Features:</b>
  1. The countdown timer counts down from 23 hours starting at when the user last received a fortune.
  2. When the countdown timer ends and the user is on the homepage, the user is sent to a [new page](#homepage---fortune) in which they can open a new fortune cookie.
  3. When the countdown timer ends (even when the user is not on the app), a push notification is sent to the user's phone, notifying them a new fortune cookie is availble for them to open.
  
### Homepage - Fortune
<p align="left"><img src="https://github.com/gmongaras/MetaU_Capstone/blob/main/Images/Homepage%20-%20Fortune%201.png" height="400" title="Homepage - Fortune">
<img src="https://github.com/gmongaras/MetaU_Capstone/blob/main/Images/Homepage%20-%20Fortune%202.png" height="400" title="Homepage - Fortune"></p>
  
- <b>Description:</b> If the user is accessing the app 23 hours after openning their last fortune, the user will be taken to this page where they can open a new fortune cookie and receive a new fortune.
- <b>Features:</b>
  1. When the fortune cookie is clicked, a little animation will play. At the end of the animation, the new fortune for the user will be displayed and saved to the user's account.
    - If the user has not granter location permissions to the app, the app will ask the user for permission as location permissions are needed to store the location the user received each fortune.
  2. After opening the cookie, if the user changed pages, the user will be greeted with a [countdown timer](#homepage---countdown) showing them how much time is left until they can open their next fortune cookie.

### Map
<p align="left"><img src="https://github.com/gmongaras/MetaU_Capstone/blob/main/Images/Map.png" height="400" title="Map"></p>
  
- <b>Description:</b> Each time the user opens a new fortune cookie, the location of the user is saved. This view shows a map with a marker at each location which the user opened a fortune at.
- <b>Features:</b>
  
  1. The location of each fortune of the logged in user is shown on the map.
  2. If one of the markers is clicked, a little discription of the fortune at that location is shown.

### Profile - Logged in user
<p align="left"><img src="https://github.com/gmongaras/MetaU_Capstone/blob/main/Images/Profile%20-%20Logged%20in%20user.png" height="400" title="Profile - Logged in user"></p>
  
- <b>Description:</b> The logged in user's profile shows the user's current fortunes and has [a menu](#profile-menu---logged-in-user) allowing the user more access to their fortunes. Each part of the menu is described in more detail below.
  
- <b>Features:</b>
  1. The user has access to [a menu](#profile-menu---logged-in-user) containing their fortunes, allowing them to search for different fortunes, like fortunes, and view their liked fortunes.
  2. If the user clicks on their profile picture, a window pops up, allowing the user to change their profile picture with one on their current device.
  3. In the top right of the view, there is a settings menu, allowing the user to change their [settings](#settings) or logout of their account, which takes them to the [login page](#login-page)

### Profile Menu - Logged in user
  
- <b>Description:</b> The logged in user's profile page has a menu with 4 views, allowing them to view their fortunes or search for fortunes.
  1. <b>Fortune List:</b> The fortune list view lists the fortunes the user owns.
     <p align="left"><img src="https://github.com/gmongaras/MetaU_Capstone/blob/main/Images/Profile%20Menu%20-%20Logged%20in%20user%201.png" title="Profile Menu - Logged in user" height="400"></p>
     - <b>Features:</b>
       1. The date the user received each fortune is shown
       2. A text snippet of each fortune is shown
       3. A heart for each fortune is either red, if the used has liked the fortune, or black, if the user has not liked the fortune.
       4. Single tapping on a fortune will go into the [detailed view](#fortune-detailed-view) of that fortune.
       5. Double tapping on a fortue will like or unlike it and will add or unadd the fortune to the user's liked list.
  2. <b>Text Search:</b> Allows the user to search for any fortunes they own by text.
     <p align="left"><img src="https://github.com/gmongaras/MetaU_Capstone/blob/main/Images/Profile%20Menu%20-%20Logged%20in%20user%202.png" title="Profile Menu - Logged in user" height="400"></p>
     - <b>Features:</b>
       1. The user can enter some text in the search bar and press enter or search to search through all their fortunes for the given text they entered.
       2. Upon searching, if fortunes were found, they will be displayed. If no fortunes were found, error text will be displayed.
       - This view also contains all features from the fortune list.
  3. <b>Location Search:</b> Allows the user to search for any fortunes they own by latitude and longitude within a given mile radius.
     <p align="left"><img src="https://github.com/gmongaras/MetaU_Capstone/blob/main/Images/Profile%20Menu%20-%20Logged%20in%20user%203.png" title="Profile Menu - Logged in user" height="400"></p>
     - <b>Features:</b>
       1. The user can enter latitude and logitude values which describes the location they want to search for fortune for. Additionally, the user can add a mile value which is the radius the user wants to query fortunes within.
       2. Upon searching, if fortunes were found withing the given radius at the given latitude and logitude values, they will be displayed. If no fortunes were found within that radius, error text will be shown.
       - This view also contains all features from the fortune list.
  4. <b>Liked List:</b> Allows the user to view any fortunes they liked, whether that's from their own fortune list or another user's fortune list.
     <p align="left"><img src="https://github.com/gmongaras/MetaU_Capstone/blob/main/Images/Profile%20Menu%20-%20Logged%20in%20user%204.png" title="Profile Menu - Logged in user" height="400"></p>
     - <b>Features</b>
       - This view contains all features from the fortune list.
  
- <b>Special Features:</b>
  - Swiping left or right will take the user to the page next to the current page.
  - Clicking on a menu item will take the user to that page

### Friends
- <b>Description:</b> The friends page has three different views allowing the user to access their friends or other users.
  1. <b>Friends List:</b> The friends list shows all the user's current friends
     <p align="left"><img src="https://github.com/gmongaras/MetaU_Capstone/blob/main/Images/Friends%201.png" title="Friends" height="400"></p>
     - <b>Features:</b>
       1. The name of each friend is shown
       2. The profile picture of each friend is shown
       3. The number of fortunes of each friend is shown
       4. Clicking on a friend takes the user to the [friend's profile](#profile---friendother-user)
       5. Pulling down from the top of the screen will reload the user's friend list.
  2. <b>Friend Requests:</b> The friends request list shows all the user's who have sent a friend request to the current user
     <p align="left"><img src="https://github.com/gmongaras/MetaU_Capstone/blob/main/Images/Friends%202.png" title="Friends" height="400"></p>
     - <b>Features:</b>
       1. The name of each user who sent a request is shown
       2. The profile picture of each user who sent a request is shown
       3. The number of fortunes of each user who sent a request is shown
       4. A button is shown to accept a friend request. Upon accepting a friend request, both users add eachother as friends and the friended user will now show up in the Friends List.
       5. A button is shown to decline a friend request. Upon declining a friend request, the users are not added as friends and the request is removed.
       6. Pulling down from the top of the screen will reload the user's friend requests.
  3. <b>Friend Search:</b> The friends search page allows the user to search for new friends
     <p align="left"><img src="https://github.com/gmongaras/MetaU_Capstone/blob/main/Images/Friends%203.png" title="Friends" height="400"></p>
     - <b>Features:</b>
       1. The search bar at the top of the page allows the user to enter text to query for new users. When the user presses the search button, users are queried. If no users were found, error text is shown. If users were found, they are displayed.
       2. Each displayed user has the name of the user shown.
       3. Each displayed user has the profile picture of the user shown.
       4. Each displayed user has the number of fortunes of that user shown.
       5. If the user is friends with the other user, clicking on the user will take the logged in user to their [friend's profile](#profile---friendother-user). If the user is not friends with the other user, clicking on the user will take the logged in user to the [other user's profile](#profile---friendother-user).
       6. A button is shown for each displayed user with the following possibilities:
          - If the current user is friends with the other user, "Already friends" is displayed without any clickable fuunctionality.
          - If the current user blocked the other user, "You blocked this user" is displayed without clickable functionality.
          - If the current user has already sent a friend request to the other user, "Remove Friend Request" is displayed allowing the user to unsend the request they send to that other user.
          - If the current user has a request from the other user, "Currently have a request" is shown without any clickable functionality.
          - If the other user is not accepting friends, "User not currently accepting friend requests" is shown without any clickable functionality.
          - If the other user blocked the current user, "This user has blocked you" is displayed without any clickacle functionality
          - If none of the other conditions are met, "Send Friend Request" is displayed, allowing the user to send a friend request to the other user.
  
- <b>Speical Features:</b>
  - Swiping left or right takes the user to the view to the left or right of the current view.
  - Clicking on a menu item takes the user to that view.

### Profile - Friend/Other User
<p align="left"><img src="https://github.com/gmongaras/MetaU_Capstone/blob/main/Images/Profile%20-%20Friend:Other%20User.png" title="Profile - Friend/Other User" height="400"></p>
  
- <b>Description:</b> The friend or other user profile shows the user's current fortunes and has [a menu](#profile-menu---friendother-user) allowing the current user more access to other user's fortunes. Each part of the menu is described in more detail below.
  
- <b>Features:</b>
  1. The user has access to [a menu](#profile-menu---friendother-user) containing the other user's fortunes, allowing them to search through that user's fortunes fortunes, like the other user's fortunes, and view the other user's liked fortunes.
  3. In the top right of the view, there is a menu:
     - For friends, this menu has a block and unfriend option. Blocking removes all liked fortunes between the current user and the friended user and also unfriends the two users on both ends. Unfriending only unfriends both users on both ends.
     - For other users, this menu only has a block option. Blocking removes all liked fortunes between the current user and the other user.

### Profile Menu - Friend/Other User
- <b>Description:</b> The other user's profile page has a menu with 4 views, allowing the current user to view the other user's fortunes or search through the other user's fortunes.
  - If the other user does not allow the current user (whether they are a friend or not a friend) to view their fortunes, none of the view will show up and error text will be displayed.
  1. <b>Fortune List:</b> The fortune list view lists the fortunes the other user owns.
     <p align="left"><img src="https://github.com/gmongaras/MetaU_Capstone/blob/main/Images/Profile%20Menu%20-%20Friend:Other%20User%201.png" title="Profile Menu - Friend/Other User" height="400"></p>
     - <b>Features:</b>
       1. The date the other user received each fortune is shown
       2. A text snippet of each fortune is shown
       3. A heart for each fortune is either red, if the current used has liked the fortune, or black, if the current user has not liked the fortune.
       4. Single tapping on a fortune will go into the [detailed view](#fortune-detailed-view) of that fortune.
       5. Double tapping on a fortue will cause the current user to like or unlike it and will add or unadd the fortune to the current user's liked list.
  2. <b>Text Search:</b> Allows the current user to search for any fortunes the other user owns by text.
     <p align="left"><img src="https://github.com/gmongaras/MetaU_Capstone/blob/main/Images/Profile%20Menu%20-%20Friend:Other%20User%202.png" title="Profile Menu - Friend/Other User" height="400"></p>
     - <b>Features:</b>
       1. The current user can enter some text in the search bar and press enter or search to search through all the other user's fortunes for the given text they entered.
       2. Upon searching, if fortunes were found, they will be displayed. If no fortunes were found, error text will be displayed.
       - This view also contains all features from the fortune list.
  3. <b>Location Search:</b> Allows the current user to search for any fortunes the other user owns by latitude and longitude within a given mile radius.
     <p align="left"><img src="https://github.com/gmongaras/MetaU_Capstone/blob/main/Images/Profile%20Menu%20-%20Friend:Other%20User%203.png" title="Profile Menu - Friend/Other User" height="400"></p>
     - <b>Features:</b>
       1. The current user can enter latitude and logitude values which describes the location they want to search the other user's fortunes for. Additionally, the user can add a mile value which is the radius the user wants to query fortunes within.
       2. Upon searching, if fortunes were found withing the given radius at the given latitude and logitude values, they will be displayed. If no fortunes were found within that radius, error text will be shown.
       - This view also contains all features from the fortune list.
  4. <b>Liked List:</b> Allows the user to view any fortunes the other user has liked, whether that's from the other user's own fortune list or another user's fortune list.
     <p align="left"><img src="https://github.com/gmongaras/MetaU_Capstone/blob/main/Images/Profile%20Menu%20-%20Friend:Other%20User%204.png" title="Profile Menu - Friend/Other User" height="400"></p>
     - <b>Features</b>
       - This view contains all features from the fortune list.
  
- <b>Special Features:</b>
  - Swiping left or right will take the current user to the page next to the current page.
  - Clicking on a menu item will take the user to that page
  - Clicking the back button will take the user back the the [Friends](#friends) page.
   
### Settings
<p align="left"><img src="https://github.com/gmongaras/MetaU_Capstone/blob/main/Images/Settings%201.png" title="Settings" height="400">
<img src="https://github.com/gmongaras/MetaU_Capstone/blob/main/Images/Settings%202.png" title="Settings" height="400"></p>
  
- <b>Description:</b> Allows the user to change their profile picture, privacy settings, application settings, and delete their account.
  
- <b>Features:</b>
  1. Clicking on the user's profile picture pops up a window allowing the user to upload an image from their device to change their profile picture.
  2. User's can set whether they want to display the app in dark mode or light mode (default)
  3. User's can change the language which the app is displayed in.
  3. The user can change their privacy/user settings:
     - User's can set whether they are allowing other users to friend them. Turning this off does not allow any other users to make new friend requests to the current user.
     - User's can set whether they are allowing friends to see their fortunes or not. Setting this off hides all fortunes when <b>friends</b> are viewing the current user's profile. This setting does not effect other users.
     - User's can set whether they are allowing other users to see their fortune or not. Setting this off hides all fortunes when <b>other users</b> are viewing the current user's profile. This setting does not effect friends.
     - User's can set whether they are allowing friends to see their map or not. Setting this off hides the map when <b>friends</b> go into the detailed view of one of the current user's fortunes. Note that if friends cannot view the current user's fortunes, this setting does not matter. This setting does not effect other users.
     - User's can set whether they are allowing other users to see their map or not. Setting this off hides the map when <b>other users</b> go into the detailed view of one of the current user's fortunes. Note that if other users cannot view the current user's fortunes, this setting does not matter. This setting does not effect friends.
  4. The user can change setting involving the app:
     - The user can choose whether they want an AI to generate fortunes or if they want to pull from real fortunes when receiving a new fortune. Turning this switch on has the AI generate fortunes while turning this switch off pulls from a list of real fortunes.
     - The user can choose whether they want push notifications from the app. If this switch is on, the user will receive a push notification 23 hours after opening a fortune cookie, but if the switch is off, the user will not receive this notification.
     - If the user has not given the app location permission, the user can do so by clicking the "Give permission" button. If the user already gave location permission, this button does nothing.
  5. The user can delete their account which unfriends the user from all other users and removes fortunes from this user. This deleting is permanent and the user has no way of getting their account back.
  6. Pressing the back button sends the user back to their [profile](#profile---logged-in-user)
  
#### Supported Languages
Below are the supported languages in the app, which can be changed in the settings or upon registration.

The list of supported lagauges by Google ML can be found at the following link: 
https://developers.google.com/ml-kit/language/identification/langid-support

Supported Languages:
- Afrikaans
- Arabic
- Belarusian
- Bulgarian
- Bengali
- Catalan
- Czech
- Welsh
- Danish
- German
- Greek
- English
- Esperanto
- Spanish
- Estonian
- Persian
- Finnish
- French
- Irish
- Galician
- Gujarati
- Hebrew
- Hindi
- Croatian
- Haitian
- Hungarian
- Indonesian
- Icelandic
- Italian
- Japanese
- Georgian
- Kannada
- Korean
- Lithuanian
- Latvian
- Macedonian
- Marathi
- Malay
- Maltese
- Dutch
- Norwegian
- Polish
- Portuguese
- Romanian
- Russian
- Slovak
- Slovenian
- Albanian
- Swedish
- Swahili
- Tamil
- Telugu
- Thai
- Turkish
- Ukrainian
- Urdu
- Vietnamese
- Chinese (Traditional)

### Fortune Detailed View
<p align="left"><img src="https://github.com/gmongaras/MetaU_Capstone/blob/main/Images/Fortune%20Detailed%20View.png" title="Fortune Detailed View" height="400"></p>
  
- <b>Description:</b> When a fortune is clicked from either the current user's profile, a friend's profile, or a different user's profile, they are taken to this view which shows the user more details on the selected fortune.
  
- <b>Features:</b>
  1. A map is shown which shows all fortunes and their location for the user this fortune belongs to. If this fortune has a location, the map is zoomed in on that location. If the user this fortune belongs to has the map turned off for the current user, the map is not shown.
  2. The time the fortune was received at is shown below the map.
  3. The entire fortune is shown below the time.
  4. The number of likes is shown below the fortune.
  5. A like button is shown next to the number of likes which allows the current user to like or unlike this fortune.
  6. A share button is shown next to the like button allowing the user to share this fortune to other apps.
  7. Pressing the back button takes the user back to the profile they were examining before going into the detail view.

# Offline Mode
When the user is offline, the following features are still available:
- A timer on the home screen shows the time left until their next fortune.
- The user's fortune list still apears as it would normally.
- The user's liked fortune list still apears as it would normally.
- A detailed view of each fortune can be access by clicking on a fortune.
- The map shows the location of each fortune.
- The dark mode/light mode theme is shown.

The following features are not available:
- A new fortune cannot be opened
- The liked state of a fortune cannot be changed.
- Friends cannot be accessed in any way.
- Settings cannot be changed.
- User's cannot search through their fortunes.
