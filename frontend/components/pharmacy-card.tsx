"use client";

import React from "react";
import {
  Card,
  CardContent,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { PharmacyDirection } from "@/lib/api";
import { MapPin, Map, Navigation, Ruler } from "lucide-react";
import { Badge } from "@/components/ui/badge";

interface PharmacyCardProps {
  pharmacy: PharmacyDirection;
}

export function PharmacyCard({ pharmacy }: PharmacyCardProps) {
  return (
    <Card className="h-full overflow-hidden transition-all duration-200 hover:shadow-md">
      <CardHeader className="pb-3 bg-primary/5">
        <CardTitle className="flex items-center gap-2 text-primary">
          <MapPin className="h-5 w-5" />
          <span className="truncate">{pharmacy.pharmacyName}</span>
        </CardTitle>
      </CardHeader>
      <CardContent className="pt-4 space-y-4">
        <div className="space-y-1">
          <div className="text-sm text-muted-foreground flex items-start gap-1.5">
            <MapPin className="h-4 w-4 shrink-0 mt-0.5" />
            <span>{pharmacy.pharmacyAddress}</span>
          </div>
          <div className="flex items-center gap-1.5">
            <Ruler className="h-4 w-4 text-primary" />
            <Badge variant="outline" className="font-medium">
              {pharmacy.distance}
            </Badge>
          </div>
        </div>
      </CardContent>
      <CardFooter className="pt-2 gap-2">
        <Button
          variant="secondary"
          size="sm"
          className="flex-1 shadow-sm"
          onClick={() => window.open(pharmacy.directionUrl, "_blank")}
        >
          <Navigation className="h-4 w-4 mr-2" />
          길찾기
        </Button>
        <Button
          variant="outline"
          size="sm"
          className="flex-1"
          onClick={() => window.open(pharmacy.roadViewUrl, "_blank")}
        >
          <Map className="h-4 w-4 mr-2" />
          로드뷰
        </Button>
      </CardFooter>
    </Card>
  );
}
