import { IParticipant } from "./participant";

export interface IRaffle {
    code: string,
    adminCode: string,
    budget: number,
    participants: IParticipant[],
    started: boolean,
    version: number
}