import { IParticipant } from "./participant";

export interface IRaffle {
    code: string,
    adminCode: string,
    participants: IParticipant[],
    started: boolean,
    version: number
}