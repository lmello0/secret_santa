import express from "express";
import { Router, Request, Response, json } from "express";
import dotenv from "dotenv";
import { DateTime } from "luxon";
dotenv.config();

import Raffle, { IRaffle } from "./database";
import { draft, sendMail } from "./utils";

const port = process.env.PORT;

const app = express();
const route = Router();

app.use(route);
route.use(json());

route.use((req: Request, res: Response, next) => {
  const now: string = DateTime.now().toFormat("dd/MM/yyyy HH:mm:ss");

  console.log(`[${req.method}] ${req.url} - ${now}`);

  next();
});

route.get("/getRaffle/:code", async (req: Request, res: Response) => {
  const { code } = req.params;

  const raffle = await Raffle.findOne({ code }).exec();

  if (raffle) {
    res.json({ raffle });
    return;
  }

  res.status(404).json({ error: "No raffle found with the given code" });
});

route.post("/createRaffle", async (req: Request, res: Response) => {
  const { code, adminCode, participants } = req.body;

  const raffleExists: boolean = (await Raffle.findOne({ code })) ? true : false;

  if (raffleExists) {
    res.status(409).json({ message: `Raffle ${code} already exists!` });
    return;
  }

  const newRaffle: IRaffle = await Raffle.create({
    code,
    adminCode,
    participants,
    started: false,
    version: 0,
  });

  res.json({ message: `Raffle ${code} created!`, raffle: newRaffle });
});

route.put("/updateRaffle", async (req: Request, res: Response) => {
  const { code, adminCode, participants, started, version } = req.body;

  const updatedRaffle = await Raffle.updateOne(
    { code },
    {
      code,
      adminCode,
      participants,
      started,
      version: version + 1,
    }
  );

  res.json({ message: `Raffle ${code} updated!`, raffle: updatedRaffle });
});

route.post("/startRaffle", async (req: Request, res: Response) => {
  const { code, adminCode, participants, version, started } = req.body;

  const raffle = await Raffle.findOneAndUpdate(
    { code },
    {
      code,
      adminCode,
      participants,
      version: Number(version) + 1,
      started: true,
    },
    { new: true,
      returnDocument: "after"
    }
  );

  const draftedParticipants = draft(participants);

  for(let i = 0; draftedParticipants.length; i++) {
    sendMail(draftedParticipants[i], code);
  }

  res.json({ message: 'Raffle started!' })
});

app.listen(port, () => {
  console.log(`Server running on port ${port}`);
});
