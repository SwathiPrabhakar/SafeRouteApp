Backend APIs

python backend, uses gmaps api.

In progress:

| type | path     | input          | output             | Description |
| ---- | -------- | -------------- | ------------------ | ----------- |
| post | /login   | [u/p]          | token              |             |
| post | /logout  | username       | resp 200           |             |
| post | /routes  | [from, to]     | routes_obj, score  |             |
| post | /history | time, location | resp_200           |             |

Todo:
add login, logout, routes, history endpoints.
push_notification Module and endpoints.
Improve prediction score metric.
Use history api data for 'freq visited places' predictions (??)


Done:
Authentication System.
History module.
Adding Gmaps api.
Naive Score and paths.

