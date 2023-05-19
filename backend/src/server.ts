import express from "express";
import { Router, Request, Response, json } from "express";
import dotenv from "dotenv";
import { DateTime } from "luxon";
dotenv.config();

import Raffle from "./database";
import { draft, sendMail } from "./utils";

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

  const raffle = Raffle.findOne({ code }, { _id: 0, 'participants._id': 0 }).exec();

  raffle
    .then((result) => {
      if (result) {
        console.log(result);

        res.send(result);
        return;
      }

      res.statusCode = 204;
      res.send("No raffle with the given code");
    })
    .catch((err) => {
      res.statusCode = 500;
      res.send(err);
    });
});

route.post("/createRaffle", async (req: Request, res: Response) => {
  const { code, adminCode, participants } = req.body;

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
    version: 0,
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

route.put("/updateRaffle", async (req: Request, res: Response) => {
  const { code, adminCode, participants } = req.body;

  let raffle = await Raffle.findOne({ code });

  if (raffle) {
    raffle.participants = participants;
    raffle.version = raffle.version + 1;

    Raffle.updateOne({ code }, raffle)
      .then(() => {
        res.statusCode = 201;
        res.json({ message: `Raffle ${code} updated!` });
      });
  } else {
    raffle = new Raffle({
      adminCode: adminCode,
      code: code,
      participants: participants,
      started: false,
      version: 0
    });

    raffle.save()
      .then(() => {
        res.statusCode = 201;
        res.json({ message: `Raffle ${code} created!` });
      });
  }
});

route.delete("/deleteRaffle/:code", async (req: Request, res: Response) => {
  const { code } = req.params;

  const raffle = await Raffle.findOne({ code }).exec();

  if (!raffle) {
    res
      .status(204)
      .json({ error: `Cannot delete inexistent raffle - ${code}` });
    return;
  }

  Raffle.deleteOne({ code }).then(() => {
    res.json({ message: `Raffle ${code} deleted` });
  });
});

route.post("/startRaffle", async (req: Request, res: Response) => {
  const { code, adminCode, participants, version } = req.body;

  await Raffle.findOneAndUpdate(
    { code },
    {
      code,
      adminCode,
      participants,
      version: Number(version) + 1,
      started: true,
    },
    { new: true }
  );

  const draftedParticipants = draft(participants);

  for (let i = 0; draftedParticipants.length; i++) {
    sendMail(draftedParticipants[i], code);
  }

  res.json({ message: "Raffle started!" });
});

app.listen(port, () => {
  console.log(`Server running on port ${port}`);
});
