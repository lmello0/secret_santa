import express from "express";
import { Router, Request, Response, json } from "express";
import dotenv from "dotenv";
import { DateTime } from "luxon";
import cors from "cors";
dotenv.config();

import Raffle from "./database";
import { draft, sendMail } from "./utils";
import { ObjectId } from "mongodb";

const port = process.env.PORT;

const app = express();
const route = Router();

// middlewares
app.use(cors());

app.use(route);

route.use(json());
route.use((req: Request, res: Response, next) => {
  const now: string = DateTime.now().toFormat("dd/MM/yyyy HH:mm:ss");

  console.log(`[${req.method}] ${req.url} - ${now}`);

  next();
});

// rotas
route.get("/getRaffle/:code", (req: Request, res: Response) => {
  const { code } = req.params;

  Raffle.findOne({ code }, { _id: 0 })
    .exec()
    .then((result) => {
      if (result) {
        res.json(result);
      } else {
        res.statusCode = 204;
        res.json("No raffle with the given code");
      }
    })
    .catch((err) => {
      res.statusCode = 500;
      res.json(err);
    });
});

route.post("/createRaffle/:code", async (req: Request, res: Response) => {
  const { code } = req.params;
  const { adminCode, participants, budget } = req.body;

  const raffleExists: Boolean = (await Raffle.findOne({ code })) ? true : false;

  if (raffleExists) {
    res.status(204).json({ message: `Raffle ${code} already exists!` });
    return;
  }

  Raffle.create({
    code,
    adminCode,
    budget,
    participants,
    started: false,
  })
    .then(() => {
      res.statusCode = 201;
      res.json({ message: `Raffle ${code} created!` });
    })
    .catch((err) => {
      console.log(err);

      res.statusCode = 204;
      res.json({ message: err });
    });
});

route.put("/updateRaffle/:code", async (req: Request, res: Response) => {
  const { code } = req.params;
  const { adminCode, participants, budget } = req.body;

  let raffle = await Raffle.findOne({ code });

  if (raffle) {
    raffle.participants = participants;

    Raffle.updateOne({ code }, raffle).then(() => {
      res.statusCode = 201;
      res.json(`Raffle ${code} updated!`);
    });
  } else {
    raffle = new Raffle({
      code,
      adminCode,
      budget,
      participants,
      started: false,
    });

    raffle.save().then(() => {
      res.statusCode = 201;
      res.json(`Raffle ${code} created!`);
    });
  }
});

route.put(
  "/updateRaffle/:code/participant/add",
  (req: Request, res: Response) => {
    const { code } = req.params;
    const participant = req.body;

    Raffle.findOne({ code }).then((result) => {
      if (result) {
        result.participants.push(participant);

        result.save();

        res.statusCode = 201;
        res.json("Participant added!");
      } else {
        res.statusCode = 204;
        res.json("No raffle found with the given code");
      }
    });
  }
);

route.put(
  "/updateRaffle/:code/participant/edit/:id",
  (req: Request, res: Response) => {
    const { code, id } = req.params;
    const { name, email } = req.body;

    Raffle.findOne({ code })
      .then((result) => {
        if (result) {
          result.participants[parseInt(id)] = { name, email };
        }

        result?.save().then(() => {
          res.json("Participant updated!");
        });
      })
      .catch((err) => {
        console.error(err);
        res.sendStatus(500);
      });
  }
);

route.delete(
  "/updateRaffle/:code/participant/remove/:id",
  (req: Request, res: Response) => {
    const { code, id } = req.params;

    Raffle.findOne({ code })
      .then((result) => {
        result?.participants.splice(parseInt(id), 1);

        result?.save().then(() => {
          res.json("Participant removed!");
        });
      })
      .catch((err) => {
        console.error(err);
        res.sendStatus(500);
      });
  }
);

route.delete("/deleteRaffle/:code", async (req: Request, res: Response) => {
  const { code } = req.params;

  const raffle = await Raffle.findOne({ code }).exec();

  if (!raffle) {
    res.status(204).send(`Cannot delete inexistent raffle - ${code}`);
    return;
  }

  Raffle.deleteOne({ code }).then(() => {
    res.json(`Raffle ${code} deleted`);
  });
});

route.post("/startRaffle", async (req: Request, res: Response) => {
  const { code, adminCode, participants } = req.body;

  await Raffle.findOneAndUpdate(
    { code },
    {
      code,
      adminCode,
      participants,
      started: true,
    },
    { new: true }
  );

  const draftedParticipants = draft(participants);

  for (let i = 0; draftedParticipants.length; i++) {
    sendMail(draftedParticipants[i], code);
  }

  res.json("Raffle started!");
});

app.listen(port, () => {
  console.log(`Server running on port ${port}`);
});
