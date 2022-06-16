# NAME

## Table of Contents
1. [Overview](#Overview)
2. [Product Spec](#Product-Spec)
3. [Wireframes](#Wireframes)
4. [Schema](#Schema)

## Overview
### Description
Users are able to get a fortune cookie a day through this app. They can then share the fortune and the fortune will be sved toa list of fortunes. Additionally, they will be able to view a map of fortunes they received and when they received it.

### App Evaluation
- **Category:** Social
- **Mobile:** This app will be developed for mobile
- **Story:** Gives fortunes to a user once a day.
- **Market:** Any individual could choose to use this app as something to do for fun for a few minuites a day and be able to view the places they visited in the past as the app marks where they got each fortune.
- **Habit:** This app could be used daily or whenever the user wants to use it depending on how consistent the user wants fortunes.
- **Scope:** First, I will start by making a basic app that is able to give fortunes. Then, I will have the fortune save to a history list. Then I will make this list sharable and have the list save the location of each fortune. Finally, I may add a friend system and some type of reward system.

## Product Spec
### 1. User Stories (Required and Optional)

**Required Must-have Stories**

* Home page that either allows the user to open a new fortune cookie, or shows the time left until a new cookie can be opened.
* A side menu that takes you to a list of past fortune cookies. This will be a recycler view
* A detailed view in the list of fortunes that allows the user to share it to Twitter or Facebook or share a link to it.
* Have a map that keeps track of the location that someone openned each fortune and then have it show up on a map. Maybe add stats as well to the map.

**Optional Nice-to-have Stories**

* Add a point system to stylize the interface.
* Daily login system to get more points
* Have friends and be able to see their list of fortunes
* Push Notification to open daily fortune

### 2. Screen Archetypes

* Register/Login - User signs in or logs into their account
    * Upon first visit this is what the user will see or when the user is not logged in.
* Home
    * Upon opening the app, the user can either open a new fortune, or if they have already done so, the interface changes to a countdown timer.
* Profile/Past Fortunes
    * A list of part fortunes is shown in this menu.
* Profile/Past Fortune - Detailed view
    * When a part of the recycler view is clicked, a detailed view is opened and the user can share their fortune and see where on the map the fortune was opened.
* Map
    * This map shows the location on a map in which each fortune was opened
* Friends
    * Shows a list of the user's friends
* Friends - Detailed View
    * Upon clicking on a friend, the user can see the friend's fortunes and their map.

### 3. Navigation

** Bottom Navigation
* Map
* Home
* Profile/Past Fortunes
* Friends

** Flow Navigation** (Screen to Screen)
* Forced Log-in -> Home page upon login/registration
* Profile -> Profile detailed view upon fortune click
* Profile Detail View -> Map, upon map click in detailed view
* Friends -> Friend detailed view upon friend click

## Wireframes
<img src="https://i.imgur.com/9CrjH1K.jpg" width=800><br>

## Schema 
### Models
#### Post

   | Property      | Type     | Description |
   | ------------- | -------- | ------------|
   | objectId      | String   | unique id for the user post (default field) |
   | author        | Pointer to User| image author |
   | image         | File     | image that user posts |
   | caption       | String   | image caption by author |
   | commentsCount | Number   | number of comments that has been posted to an image |
   | likesCount    | Number   | number of likes for the post |
   | createdAt     | DateTime | date when post is created (default field) |
   | updatedAt     | DateTime | date when post is last updated (default field) |
### Networking
#### List of network requests by screen
   - Home Feed Screen
      - (Read/GET) Query all posts where user is author
         ```swift
         let query = PFQuery(className:"Post")
         query.whereKey("author", equalTo: currentUser)
         query.order(byDescending: "createdAt")
         query.findObjectsInBackground { (posts: [PFObject]?, error: Error?) in
            if let error = error { 
               print(error.localizedDescription)
            } else if let posts = posts {
               print("Successfully retrieved \(posts.count) posts.")
           // TODO: Do something with posts...
            }
         }
         ```
      - (Create/POST) Create a new like on a post
      - (Delete) Delete existing like
      - (Create/POST) Create a new comment on a post
      - (Delete) Delete existing comment
   - Create Post Screen
      - (Create/POST) Create a new post object
   - Profile Screen
      - (Read/GET) Query logged in user object
      - (Update/PUT) Update user profile image
#### [OPTIONAL:] Existing API Endpoints
##### An API Of Ice And Fire
- Base URL - [http://www.anapioficeandfire.com/api](http://www.anapioficeandfire.com/api)

   HTTP Verb | Endpoint | Description
   ----------|----------|------------
    `GET`    | /characters | get all characters
    `GET`    | /characters/?name=name | return specific character by name
    `GET`    | /houses   | get all houses
    `GET`    | /houses/?name=name | return specific house by name

##### Game of Thrones API
- Base URL - [https://api.got.show/api](https://api.got.show/api)

   HTTP Verb | Endpoint | Description
   ----------|----------|------------
    `GET`    | /cities | gets all cities
    `GET`    | /cities/byId/:id | gets specific city by :id
    `GET`    | /continents | gets all continents
    `GET`    | /continents/byId/:id | gets specific continent by :id
    `GET`    | /regions | gets all regions
    `GET`    | /regions/byId/:id | gets specific region by :id
    `GET`    | /characters/paths/:name | gets a character's path with a given name
