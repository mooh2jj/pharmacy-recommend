import {
  Card,
  CardContent,
  CardFooter,
  CardHeader,
} from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";

export function PharmacySkeleton() {
  return (
    <Card className="h-full overflow-hidden transition-all duration-200">
      <CardHeader className="pb-3 bg-primary/5">
        <Skeleton className="h-6 w-3/4" />
      </CardHeader>
      <CardContent className="pt-4 space-y-4">
        <div className="space-y-2">
          <Skeleton className="h-4 w-full" />
          <Skeleton className="h-4 w-4/5" />
          <div className="mt-2 flex items-center gap-1.5">
            <Skeleton className="h-4 w-4" />
            <Skeleton className="h-5 w-20 rounded-full" />
          </div>
        </div>
      </CardContent>
      <CardFooter className="pt-2 gap-2">
        <Skeleton className="h-9 w-full" />
        <Skeleton className="h-9 w-full" />
      </CardFooter>
    </Card>
  );
}
