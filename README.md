# Overview
- [Project Description](#project-description)
- [Project Requirements](#project-requirements)
   - [Java Packages](#)
   - [APIs used](#)
- [Cloning The Project](#cloning-the-project)
- [Executing The Project](#executing-the-project)
- [View Descriptions](#view-descriptions)
  - [Login View](#)


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



# Cloning The Project



# Executing The Project



# View Descriptions
There are a lot of views in this project, and I will go over each one below.

## View Table of Contents
- [Login Page]()

### Login Page
- <b>Description:</b> When the user firsts starts up the app, the user is greeted with a login page to log the user into the app. 
The user account is used to keep track of the user's fortunes, friends, etc.
- <b>Features:</b>
  1. The user is able to enter a username and password.
  2. A Login button allows the user to login to their account if the username exists and the password is correct. Upon login, the user is taken to their [homepage](#homepage-1---fortune)
  3. If the user doesn't have an account, the user can [register](#register-page) on a different page

### Register Page
- <b>Description:</b> If the user wants to make a new account, they can do so by entering a new username and a new password.
- <b>Features:</b>
  1. The user is able to enter a username which will be used as that user's account username.
  2. The user can enter a password and reenter that password to create a new password for their new account.
  3. If the passwords match and the username is not already taken, a new user is created with the specified username as their new username and the specified password as their new password. The user is then logged into their account and sent to their [homepage](#homepage-1---fortune)
  
### Homepage 1 - Fortune
