# Class Exam Instruction: Building a ZIO Application Backend

## Description

This is a really simple REST API showing information about baseball matches and prediction about wether a team would win or loose.

It uses [scala-csv](https://github.com/tototoshi/scala-csv) and [ZIO](https://zio.dev/overview/getting-started) to read a CSV file and create a database filled by the data of the CSV file. The ZIO-jdbc library permits us to execute actions on the database, such as `CREATE`, `INSERT`, `SELECT`...  

Then, the ZIO-json library is used to return in a readable json format the data retrievied from our database.

### Contributors

- [@Turdyo](https://github.com/Turdyo)
- [@maxblt](https://github.com/maxblt)
- [@Faust1-2](https://github.com/Faust1-2)
- [@Tenkoh](https://github.com/BlanchardNicolas)

## Dataset Description
The "Major League Baseball Dataset" from Kaggle is a comprehensive collection of data related to Major League Baseball (MLB) games, players, teams, and statistics. The dataset contains information about game-by-game Elo ratings and forecasts back to 1871. You can visit the [Kaggle](https://www.kaggle.com/datasets/saurabhshahane/major-league-baseball-dataset) page for a more detailed description of the dataset. 

Since the dataset is to heavy to fit in a git repository, **you need to download the dataset into the `./csv/` folder.** The two needed files are `mlb_elo.csv`, which contains all the data since 1871, and `mlb_elo_latest.csv`, that contains the data of the 2021 season.

The dataset is composed of 26 columns, whit a few of them having null values.

## Initialisation and running of the project

In order to run the project, you will need to run specific commands in the following order :

- This command will make you connect to the sbt build server. It will load all the dependencies stored in the `build.sbt` file.
  ```
  sbt
  ```

- You can now run the project with:
  ```
  ~reStart
  ```
- You can also directly use:
  ```
  sbt ~reStart
  ```

## Endpoints

As a rest API, this application serve differents endpoints. We can split them into different kinds of endpoints.

### Predictions

- **`predict/elo/${gameId}`** : Returns the winning team of one game, according to the elo_prob columns. The `${gameId}` must be replaced by an Int, otherwise there will be no result.
- **`predict/rating/${gameId}`** : Returns the winning team of one game, according to the rating_rprob columns. The `${gameId}` must be replaced by an Int, otherwise there will be no result.

### Matches

- **`matches`** : Return the history of the games. The history shows the id, the date of the match, the teams acronyms, and the teams scores. 
- **`matches/season/${seasonYear}`**: Return the history of the games in one season.
- **`matches/{matchId}`**: Return the id, the date, teams acronyms and teams scores of the specified match.


## Ratings Systems: ELO and MLB Predictions
The dataset includes two ratings systems, ELO and MLB Predictions, which are used to evaluate teams' performance and predict game outcomes:

1. **ELO**: The ELO rating system is a method for calculating the relative skill levels of teams in two-player games, such as chess. In the context of MLB, the ELO rating system assigns a numerical rating to each team, which reflects their relative strength. The rating is updated based on game outcomes, with teams gaining or losing points depending on the result of the match.

2. **MLB Predictions**: The MLB Predictions rating system utilizes various statistical models and algorithms to predict game outcomes. It takes into account factors such as team performance, player statistics, historical data, and other relevant factors to generate predictions for upcoming games.