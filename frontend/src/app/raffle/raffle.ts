import { IParticipant } from "./participant";

export interface IRaffle {
    code: String,
    adminCode: String,
    participants: IParticipant[],
    started: Boolean,
    version: Number
}