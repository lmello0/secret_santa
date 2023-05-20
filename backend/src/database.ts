import { Schema, model, connect } from 'mongoose';

export interface IParticipant {
    id?: number,
    name: string,
    email: string
}

export interface IRaffle {
    code: string,
    adminCode: string,
    budget: number,
    participants: [IParticipant],
    started: boolean,
    version: number
}

const MONGO_URI: string = process.env.DB_CONNSTRING!;

const participantsSchema = new Schema<IParticipant>({
    name: { type: String, required: true },
    email: { type: String, required: true }
});

const raffleSchema = new Schema<IRaffle>({
    code: { type: String, required: true, unique: true },
    adminCode: { type: String, required: true },
    budget: { type: Number, required: true },
    participants: { type: [participantsSchema], default: [] },
    started: { type: Boolean, default: false }
});

const Raffle = model<IRaffle>('Raffle', raffleSchema);

async function run() {
    await connect(MONGO_URI);
}

run()
    .then(() => { console.log('Connected to MongoDB') })
    .catch(err => {
            console.error(err);
            process.exit;
        })

export default Raffle;