
### Backend APIs

python backend, uses gmaps api.

In progress:

| type | path     | input          | output             | Description |
| ---- | -------- | -------------- | ------------------ | ----------- |
| post | /login   | [u/p]          | token              |             |
| post | /logout  | username       | resp 200           |             |
| post | /routes  | [from, to]     | routes_obj, score  |             |
| post | /history | time, location | resp_200           |             |

Todo:
* add login, logout, routes, history endpoints.
* push_notification Module and endpoints.
* Route Prediction: Improve prediction score metric. [+ Traffic metric] 
* Location prediction: Use history api data for 'freq visited places' predictions (??) (along with * Sunset/Sunrise time)
* Starred location API
* Alert Friend API
* return routes along with crime markers in api
* return routes along with Traffic data marker in api


Done:
* Authentication System.
* History module.
* Adding Gmaps api.
* Naive Score and paths.

Notes:
Using SF data currently.

### Mobile App:
Todos:
Mobile App tasks:
* From, To, Map
* Plot a Route, will be sent from backend API (doubtful)
* Link with Backend API
* Login, profile, page
* History service
* Push notifications receiving
* Starred Location
* Alert Friend
* Custom Markers for crime (doubtful)
* Custom Markers for Traffic conditions (doubtful)