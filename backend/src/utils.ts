import { IParticipants } from "./database";
import { createTransport } from "nodemailer";
import { hashSync, genSaltSync, hash } from "bcryptjs";

interface IDraft {
    sender: IParticipants,
    recipient: IParticipants
}

const transporter = createTransport({
    host: process.env.EMAIL_HOST,
    port: Number(process.env.EMAIL_PORT),
    secure: true,
    auth: {
        user: process.env.EMAIL_USER,
        pass: process.env.EMAIL_PASS
    },
    tls: {
        rejectUnauthorized: false
    }
});

export function draft(participants: IParticipants[]): IDraft[] {
    let recipients: IParticipants[] = [];

    let cont: number = 0;
    while(cont == 0) {
        let redo: boolean = false;
        let possibleSanta: IParticipants[] = participants;

        const draftLength: number = possibleSanta.length;

        for(let i: number = 0; i < draftLength; i ++) {
            let recipIdx: number = Math.floor(Math.random() * possibleSanta.length);

            let x: number = 0;
            while(x == 0) {
                if (participants[i] == possibleSanta[recipIdx]) {
                    if (possibleSanta.length == 1) {
                        redo = true;
                        x = 1;
                    } else {
                        recipIdx = Math.floor(Math.random() * possibleSanta.length);
                    }
                } else {
                    x = 1;
                }
            }

            if (!redo) {
                recipients.push(possibleSanta[recipIdx]);
                possibleSanta.splice(recipIdx, 1);
                cont = 1;
            } else {
                cont = 0;
            }
        }
    }

    let returnData: IDraft[] = [];
    participants.forEach((participant, index) => {
        returnData.push({
            sender: { name: participant.name, email: participant.email },
            recipient: { name: recipients[index].name, email: recipients[index].email }
        });
    });

    return returnData;
}

export function sendMail(draft: IDraft, code: string): void {
    const mailData = {
        subject: `Pessoa sorteada: [${code}]`,
        text: `[${code}]\n\nVocê tirou ${draft.recipient.name}`,
        to: draft.sender.email
    }

    transporter.sendMail(mailData);
}

export async function hashString(stringToHash: string) {
    const salt = genSaltSync(10);
    const hash = hashSync(stringToHash, salt);

    return hash;
}