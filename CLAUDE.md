# ParkLight — Development Guide

## Identity
- Git commits and `@author` tags must always use: **Tal Almagor <talmagor@mishloha.co.il>**

## Project Overview
- Final project for the Advanced Java course at HIT.
- A smart parking lot system with shortest-path-based slot allocation.
- Tagline: *Light the way to your spot.*
- Two-module project:
  - `AlgorithmModule/` — Strategy-pattern library of shortest-path algorithms (Part A).
  - `ParkLightApp/` — The application that uses the library (Part B).

## Domain
- Spot types: `REGULAR`, `DISABLED`, `ELECTRIC`.
- Vehicle types: `REGULAR`, `DISABLED`, `ELECTRIC`.
- The parking lot is modeled as a weighted graph (entrance + spots + intersections, edges = distances).
- The shortest-path algorithm picks the closest available compatible spot for each incoming vehicle.

## Technical Constraints
- Java 8 source level (`JavaSE-1.8`).
- JUnit 4 for unit tests.
- **No external frameworks** — no Spring, no Maven build, no React, no Angular, no Lombok.
- Build with plain `javac` and `jar` only.

## Package Layout
- `com.parklight.algorithm` — Part A.
- `com.parklight.dao`, `com.parklight.dm`, `com.parklight.service` — Part B.

## Part A — Algorithm Module (current state)
| File | Role |
|---|---|
| `IAlgoShortestPath<T>` | Interface (3 methods: `addEdge`, `findShortestPath`, `getDistance`) |
| `AbstractAlgoShortestPath<T>` | Holds the graph, shares helpers (`neighborsOf`, `buildPath`) |
| `DijkstraAlgoImpl<T>` | Dijkstra's algorithm |
| `AStarAlgoImpl<T>` | A* with a `BiFunction<T,T,Double>` heuristic |

## Style Rules (CRITICAL — the lecturer checks for AI-generated style)
- Comments **short** and in **English**.
- Method-level comments: plain `//` only, no JavaDoc per method.
- Class-level JavaDoc: 2–4 lines, with `@param` and `@author Tal Almagor`.
- **Avoid these AI tells:**
  - Philosophical or metaphorical phrasing ("best understood as", "boring-but-shared", "the secret sauce").
  - Adjectives like "elegant", "robust", "lightweight", "production-ready", "comprehensive", "powerful".
  - Em-dashes (`—`); use a regular dash `-` or rephrase.
  - Excessive `{@link X}` cross-references.
  - Naming design patterns in comments (e.g. don't say "Strategy Pattern" in JavaDoc).
  - Over-modular helpers and defensive programming for unlikely cases.
- Aim for real-student feel: practical, focused, only commented where the comment actually adds value.

## Workflow Rules
1. After **every** file change, verify it compiles: `javac -d /tmp/build <changed file + its deps>`.
2. **One logical change = one commit** (small, granular history).
3. Commit messages: present tense, plain English — "Add X", "Implement Y", "Wire Z to W".
4. After every commit: `git push origin main`.
5. Never rewrite published history (no `--amend`, no force-push) unless I explicitly ask.

## Status
- ✅ Part A: interface, abstract, Dijkstra, A*, JUnit lib in place.
- ⏳ Part A: JUnit test file (`IAlgoShortestPathTest`).
- ⏳ Part A: package AlgorithmModule as a JAR.
- ✅ Part B: skeleton (`ParkLightApp/` directory tree, JAR in lib/, empty datasource.txt).
- ⏳ Part B: DataModels (`Vehicle`, `ParkingSpot`, `ParkingTicket`, `SpotType`, `VehicleType`).
- ⏳ Part B: DAO layer (`IDao`, `DaoFileImpl`).
- ⏳ Part B: Services (`ParkingService`, `BillingService`).
- ⏳ Part B: End-to-end test (`ParkLightServiceTest`).

## Working With Me
- I'll send you task-specific prompts one at a time.
- Treat each prompt as a single focused unit of work; do not jump ahead to the next task.
- If a prompt looks ambiguous or risky, ASK before acting.
