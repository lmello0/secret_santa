import { Schema, model, connect } from 'mongoose';

export interface IParticipants {
    name: string,
    email: string
}

export interface IRaffle {
    code: string,
    adminCode: string,
    participants: [IParticipants],
    started: boolean,
    version: number
}

const MONGO_URI: string = process.env.DB_CONNSTRING!;

const participantsSchema = new Schema<IParticipants>({
    name: { type: String, required: true },
    email: { type: String, required: true }
});

const raffleSchema = new Schema<IRaffle>({
    code: { type: String, required: true, unique: true },
    adminCode: { type: String, required: true },
    participants: { type: [participantsSchema], default: [] },
    started: { type: Boolean, default: false },
    version: { type: Number, default: 0 }
}, { versionKey: 'version' });

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