# Modak challenge

## Prompt

We have a Notification system that sends out email notifications of various types (supdatesupdate, daily news, project invitations, etc). We need to protect recipients from getting too many emails, either due to system errors or due to abuse, so let's limit the number of emails sent to them by implementing a rate-limited version of NotificationService.

The system must reject requests that are over the limit.

Some sample notification types and rate limit rules, e.g.:

    Status: not more than 2 per minute for each recipient

    News: not more than 1 per day for each recipient

    Marketing: not more than 3 per hour for each recipient

    Etc. these are just samples, the system might have several rate limit rules!

## Solution

### About the RateLimiter
For the solution I choose to use a bucket based rate limiter, as that scheme resonated with the given examples.

In this case, the rate limiter allows for two configuration parameters, period and amount.

This means that for a certain period, expressed as a duration of time, the specified amount of events will be allowed.
The period starts when the first event is allowed through. The amount of allowed tasks is kept as a counter decremented on each allowed event
At the end of `period` the counter is reset to `amount`.

RateLimiter is abstracted from the keys uses to separate counter domains. This means that the callers of RateLimiter need to specify how each event relates to a single counter.
In the case of Notifications, a separated counter is kept for each (user, notification kind) pair. 

### About the NotificationService
The NotificationService trait (interface) is thought to follow a decorator pattern. 
This would allow to build different stacks of notifications if based on different criterias, for example, routing or storing sent notifications, or any other use case

## Running the code

There is a demo main application that will send a notification every 200ms and only allow 3 notifications every 2 seconds

To run it you can use the provided Dockerfile

```
docker run --rm -it $(docker build -q .) 
```

