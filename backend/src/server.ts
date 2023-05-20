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
app.use(route);
route.use(json());

route.use((req: Request, res: Response, next) => {
  const now: string = DateTime.now().toFormat("dd/MM/yyyy HH:mm:ss");

  console.log(`[${req.method}] ${req.url} - ${now}`);

  next();
});

route.use((req: Request, res: Response, next) => {
  res.set({
    "Access-Control-Allow-Origin": "*",
    "Access-Control-Allow-Methods": "*",
    "Access-Control-Allow-Headers": "*",
  });

  next();
});

// rotas
route.get("/getRaffle/:code", (req: Request, res: Response) => {
  const { code } = req.params;

  Raffle.findOne({ code }, { _id: 0 }).exec()
    .then((result) => {
      if (result) {
        res.send(result);
      } else {
        res.statusCode = 204;
        res.send("No raffle with the given code");
      }
    })
    .catch((err) => {
      res.statusCode = 500;
      res.send(err);
    });
});

route.post("/createRaffle/:code", async (req: Request, res: Response) => {
  const { code } = req.params;
  const { adminCode, participants } = req.body;

  const raffleExists: Boolean = (await Raffle.findOne({ code })) ? true : false;

  if (raffleExists) {
    res.status(204).json({ message: `Raffle ${code} already exists!` });
    return;
  }

  Raffle.create({
    code,
    adminCode,
    participants,
    started: false,
  })
    .then(() => {
      res.statusCode = 201;
      res.json({ message: `Raffle ${code} created!` });
    })
    .catch((err) => {
      res.statusCode = 204;
      res.json({ message: err });
    });
});

route.put("/updateRaffle/:code", async (req: Request, res: Response) => {
  const { code } = req.params;
  const { adminCode, participants } = req.body;

  let raffle = await Raffle.findOne({ code });

  if (raffle) {
    raffle.participants = participants;

    Raffle.updateOne({ code }, raffle).then(() => {
      res.statusCode = 201;
      res.send(`Raffle ${code} updated!`);
    });
  } else {
    raffle = new Raffle({
      adminCode: adminCode,
      code: code,
      participants: participants,
      started: false,
    });

    raffle.save().then(() => {
      res.statusCode = 201;
      res.send(`Raffle ${code} created!`);
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
        res.send("Participant added!");
      } else {
        res.statusCode = 204;
        res.send("No raffle found with the given code");
      }
    });
  }
);

route.put(
  "/updateRaffle/:code/participant/remove",
  (req: Request, res: Response) => {
    const { code } = req.params;
    const participant = req.body;

    Raffle.findOneAndUpdate(
      { code, participants: { $elemMatch: participant } },
      { $pull: { participants: participant } },
      { new: true }
    ).then((result) => {
      if (result) {
        res.send('Participant removed!');
      } else {
        res.statusCode = 204;
        res.send('Document or object not found');
      }
    })
    .catch((err) => {
      res.statusCode = 500;
      res.send('Server error');
    });
  }
);

route.delete("/deleteRaffle/:code", async (req: Request, res: Response) => {
  const { code } = req.params;

  const raffle = await Raffle.findOne({ code }).exec();

  if (!raffle) {
    res
      .status(204)
      .send(`Cannot delete inexistent raffle - ${code}`);
    return;
  }

  Raffle.deleteOne({ code }).then(() => {
    res.send(`Raffle ${code} deleted`);
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

  res.send("Raffle started!");
});

app.listen(port, () => {
  console.log(`Server running on port ${port}`);
});
